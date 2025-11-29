package com.cloud.code_sage_analyzer.analyzer;

import com.cloud.code_sage_model.analyzer.vo.AnalysisResult;
import com.cloud.code_sage_model.analyzer.vo.BugFinding;
import com.cloud.code_sage_model.analyzer.vo.DefUseInfo;
import com.cloud.code_sage_model.analyzer.vo.MetricDetail;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Position;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.*;

@Component
public class JavaAnalyzer {
    public AnalysisResult analyze(String code) {
        AnalysisResult result = new AnalysisResult();
        Map<String, Integer> metrics = new HashMap<>();
        List<MetricDetail> methodMetrics = new ArrayList<>();
        List<BugFinding> findings = new ArrayList<>();
        List<DefUseInfo> defUseInfos = new ArrayList<>();

        ParserConfiguration config = new ParserConfiguration();
        JavaParser parser = new JavaParser(config);

        ParseResult<CompilationUnit> parseResult = parser.parse(new StringReader(code));
        if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
            // parse error -> return minimal info
            metrics.put("LOC", countLines(code));
            result.setMetrics(metrics);
            result.setFindings(findings);
            result.setMethodMetrics(methodMetrics);
            result.setDefUseInfos(defUseInfos);
            return result;
        }

        CompilationUnit cu = parseResult.getResult().get();

        // file LOC
        metrics.put("LOC", countLines(code));

        // method-level analysis
        cu.findAll(MethodDeclaration.class).forEach(md -> {
            MetricDetail mdDetail = new MetricDetail();
            String methodName = md.getNameAsString();
            mdDetail.setName(methodName);
            int loc = calcLoc(md);
            mdDetail.setLoc(loc);

            CyclomaticAndNestingVisitor ccVisitor = new CyclomaticAndNestingVisitor();
            ccVisitor.visit(md, null);
            mdDetail.setCyclomaticComplexity(ccVisitor.getCyclomatic());
            mdDetail.setMaxNestingDepth(ccVisitor.getMaxNesting());

            methodMetrics.add(mdDetail);

            // def-use within method
            DefUseVisitor defUseVisitor = new DefUseVisitor(methodName);
            defUseVisitor.visit(md, null);
            defUseInfos.addAll(defUseVisitor.buildDefUseInfos());

            // bug pattern checks inside method
            findings.addAll(defUseVisitor.getFindings());
            // also check loops/catch inside method
            LoopAndCatchVisitor lcVisitor = new LoopAndCatchVisitor();
            lcVisitor.visit(md, null);
            findings.addAll(lcVisitor.getFindings());
        });

        // also check array-access across the compilation unit (simple heuristic)
        ArrayAccessVisitor aaVisitor = new ArrayAccessVisitor();
        aaVisitor.visit(cu, null);
        findings.addAll(aaVisitor.getFindings());

        // aggregate
        metrics.put("total_methods", methodMetrics.size());
        int totalCC = methodMetrics.stream().mapToInt(MetricDetail::getCyclomaticComplexity).sum();
        metrics.put("total_cyclomatic", totalCC);
        int maxNesting = methodMetrics.stream().mapToInt(MetricDetail::getMaxNestingDepth).max().orElse(0);
        metrics.put("max_nesting", maxNesting);

        result.setMetrics(metrics);
        result.setMethodMetrics(methodMetrics);
        result.setFindings(findings);
        result.setDefUseInfos(defUseInfos);
        return result;
    }

    private int countLines(String code) {
        return (int) code.lines().count();
    }

    private int calcLoc(Node node) {
        Optional<Position> begin = node.getBegin();
        Optional<Position> end = node.getEnd();
        if (begin.isPresent() && end.isPresent()) {
            return end.get().line - begin.get().line + 1;
        }
        return 0;
    }

    /**
     * Visitor: compute cyclomatic complexity (heuristic) & nesting depth
     */
    private static class CyclomaticAndNestingVisitor extends VoidVisitorAdapter<Void> {
        private int cyclomatic = 1; // base 1
        private int currentNesting = 0;
        private int maxNesting = 0;

        public int getCyclomatic() { return cyclomatic; }
        public int getMaxNesting() { return maxNesting; }

        @Override
        public void visit(IfStmt n, Void arg) {
            cyclomatic++; // each if increases CC
            enterNesting(() -> super.visit(n, arg));
            // else-if: JavaParser produces nested if in else branch, so handled
        }

        @Override
        public void visit(ForStmt n, Void arg) {
            cyclomatic++;
            enterNesting(() -> super.visit(n, arg));
        }

        @Override
        public void visit(ForEachStmt n, Void arg) {
            cyclomatic++;
            enterNesting(() -> super.visit(n, arg));
        }

        @Override
        public void visit(WhileStmt n, Void arg) {
            cyclomatic++;
            enterNesting(() -> super.visit(n, arg));
        }

        @Override
        public void visit(DoStmt n, Void arg) {
            cyclomatic++;
            enterNesting(() -> super.visit(n, arg));
        }

        @Override
        public void visit(SwitchEntry n, Void arg) {
            // each case (except default) increments CC
            if (!n.getLabels().isEmpty()) {
                cyclomatic += n.getLabels().size();
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(ConditionalExpr n, Void arg) {
            cyclomatic++;
            super.visit(n, arg);
        }

        @Override
        public void visit(CatchClause n, Void arg) {
            cyclomatic++;
            enterNesting(() -> super.visit(n, arg));
        }

        private void enterNesting(Runnable r) {
            currentNesting++;
            maxNesting = Math.max(maxNesting, currentNesting);
            r.run();
            currentNesting--;
        }
    }

    /**
     * Simple def-use visitor: detect variable declarations (defs) and uses (NameExpr)
     * Also detect uninitialized variable usage (very conservative heuristic)
     */
    private static class DefUseVisitor extends VoidVisitorAdapter<Void> {
        private final String methodName;
        private final Map<String, List<Integer>> defs = new HashMap<>();
        private final Map<String, List<Integer>> uses = new HashMap<>();
        private final List<BugFinding> findings = new ArrayList<>();
        private final Set<String> declaredWithoutInit = new HashSet<>();
        private final Set<String> initialized = new HashSet<>();

        public DefUseVisitor(String methodName) {
            this.methodName = methodName;
        }

        public List<DefUseInfo> buildDefUseInfos() {
            List<DefUseInfo> out = new ArrayList<>();
            for (String var : unionKeys()) {
                DefUseInfo info = new DefUseInfo();
                info.setMethodName(methodName);
                info.setVariable(var);
                info.setDefLines(defs.getOrDefault(var, List.of()));
                info.setUseLines(uses.getOrDefault(var, List.of()));
                info.setUsed(!uses.getOrDefault(var, List.of()).isEmpty());
                out.add(info);
                // flag not used
                if (!info.isUsed()) {
                    BugFinding b = new BugFinding();
                    b.setKind("UnusedVariable");
                    b.setMessage("Variable '" + var + "' declared but not used in method " + methodName);
                    List<Integer> dd = info.getDefLines();
                    b.setLine(dd.isEmpty() ? -1 : dd.get(0));
                    findings.add(b);
                }
            }
            return out;
        }

        private Set<String> unionKeys() {
            Set<String> s = new HashSet<>();
            s.addAll(defs.keySet());
            s.addAll(uses.keySet());
            return s;
        }

        public List<BugFinding> getFindings() {
            // after visiting, check uninitialized used variables
            for (String var : uses.keySet()) {
                if (declaredWithoutInit.contains(var) && !initialized.contains(var)) {
                    BugFinding b = new BugFinding();
                    b.setKind("PossiblyUninitialized");
                    b.setMessage("Variable '" + var + "' may be used before initialization in method " + methodName);
                    b.setLine(uses.get(var).get(0));
                    findings.add(b);
                }
            }
            return findings;
        }

        @Override
        public void visit(VariableDeclarationExpr n, Void arg) {
            n.getVariables().forEach(v -> {
                String name = v.getNameAsString();
                int line = v.getBegin().map(p -> p.line).orElse(-1);
                defs.computeIfAbsent(name, k -> new ArrayList<>()).add(line);
                if (v.getInitializer().isPresent()) {
                    initialized.add(name);
                } else {
                    declaredWithoutInit.add(name);
                }
            });
            super.visit(n, arg);
        }

        @Override
        public void visit(AssignExpr n, Void arg) {
            // left side assigned -> mark initialized
            Expression target = n.getTarget();
            if (target.isNameExpr()) {
                String name = target.asNameExpr().getNameAsString();
                initialized.add(name);
            } else if (target.isFieldAccessExpr()) {
                // ignore fields for method-level
            } else if (target.isArrayAccessExpr()) {
                // ignore
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(NameExpr n, Void arg) {
            String name = n.getNameAsString();
            int line = n.getBegin().map(p -> p.line).orElse(-1);
            uses.computeIfAbsent(name, k -> new ArrayList<>()).add(line);
            super.visit(n, arg);
        }

        // Detect some simple array-out-of-bounds patterns like:
        // int[] a = new int[3]; a[5] -> index literal >= size
        @Override
        public void visit(ArrayAccessExpr n, Void arg) {
            Expression index = n.getIndex();
            Expression array = n.getName();
            // if array is a variable declared in same method: check initializer
            if (array.isNameExpr()) {
                String arrName = array.asNameExpr().getNameAsString();
                // try find var decl in defs (we don't have full map to sizes here)
                // leave to ArrayAccessVisitor for stronger heuristics
            }
            super.visit(n, arg);
        }
    }

    /**
     * Loop & Catch visitor to detect infinite loops and empty catch blocks
     */
    private static class LoopAndCatchVisitor extends VoidVisitorAdapter<Void> {
        private final List<BugFinding> findings = new ArrayList<>();

        public List<BugFinding> getFindings() { return findings; }

        @Override
        public void visit(WhileStmt n, Void arg) {
            // detect while(true) or while(1==1)
            Expression cond = n.getCondition();
            if (isAlwaysTrue(cond)) {
                // heuristics: if body contains break/return throw maybe not infinite
                boolean hasBreak = n.findAll(BreakStmt.class).size() > 0;
                boolean hasReturn = n.findAll(ReturnStmt.class).size() > 0;
                boolean hasThrow = n.findAll(ThrowStmt.class).size() > 0;
                if (!hasBreak && !hasReturn && !hasThrow) {
                    BugFinding b = new BugFinding();
                    b.setKind("InfiniteLoop");
                    b.setMessage("Potential infinite loop (while(true)/always-true) without break/return/throw.");
                    b.setLine(n.getBegin().map(p -> p.line).orElse(-1));
                    findings.add(b);
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(DoStmt n, Void arg) {
            // similar heuristics
            Expression cond = n.getCondition();
            if (isAlwaysTrue(cond)) {
                boolean hasBreak = n.findAll(BreakStmt.class).size() > 0;
                boolean hasReturn = n.findAll(ReturnStmt.class).size() > 0;
                boolean hasThrow = n.findAll(ThrowStmt.class).size() > 0;
                if (!hasBreak && !hasReturn && !hasThrow) {
                    BugFinding b = new BugFinding();
                    b.setKind("InfiniteLoop");
                    b.setMessage("Potential infinite do-while loop with always-true condition.");
                    b.setLine(n.getBegin().map(p -> p.line).orElse(-1));
                    findings.add(b);
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(ForStmt n, Void arg) {
            if (!n.getCompare().isPresent()) {
                // for(;;) is infinite
                boolean hasBreak = n.findAll(BreakStmt.class).size() > 0;
                boolean hasReturn = n.findAll(ReturnStmt.class).size() > 0;
                boolean hasThrow = n.findAll(ThrowStmt.class).size() > 0;
                if (!hasBreak && !hasReturn && !hasThrow) {
                    BugFinding b = new BugFinding();
                    b.setKind("InfiniteLoop");
                    b.setMessage("Potential infinite for(;;) loop without break/return/throw.");
                    b.setLine(n.getBegin().map(p -> p.line).orElse(-1));
                    findings.add(b);
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(CatchClause n, Void arg) {
            // empty catch or just commentless swallow
            BlockStmt body = n.getBody();
            boolean hasStatements = !body.getStatements().isEmpty();
            if (!hasStatements) {
                BugFinding b = new BugFinding();
                b.setKind("EmptyCatch");
                b.setMessage("Empty catch block swallows exception: " + n.getParameter().getType());
                b.setLine(n.getBegin().map(p -> p.line).orElse(-1));
                findings.add(b);
            } else {
                // catch with only logging? (we don't inspect content deeply here)
            }
            super.visit(n, arg);
        }

        private boolean isAlwaysTrue(Expression cond) {
            if (cond.isBooleanLiteralExpr()) {
                return cond.asBooleanLiteralExpr().getValue();
            }
            if (cond.isBinaryExpr()) {
                BinaryExpr be = cond.asBinaryExpr();
                // 1==1 style
                Optional<Integer> left = constantInt(be.getLeft());
                Optional<Integer> right = constantInt(be.getRight());
                if (left.isPresent() && right.isPresent()) {
                    switch (be.getOperator()) {
                        case EQUALS: return left.get().intValue() == right.get().intValue();
                        case NOT_EQUALS: return left.get().intValue() != right.get().intValue();
                        default: return false;
                    }
                }
            }
            return false;
        }

        private Optional<Integer> constantInt(Expression e) {
            if (e.isIntegerLiteralExpr()) {
                try {
                    return Optional.of(Integer.parseInt(e.asIntegerLiteralExpr().getValue()));
                } catch (NumberFormatException ignore) {}
            }
            return Optional.empty();
        }
    }

    /**
     * ArrayAccessVisitor: tries to detect simple literal-index OOB if array size is known inline
     */
    private static class ArrayAccessVisitor extends VoidVisitorAdapter<Void> {
        private final List<BugFinding> findings = new ArrayList<>();
        // Map var name -> known length (if initialized as new int[NN])
        private final Map<String, Integer> arraySizes = new HashMap<>();

        public List<BugFinding> getFindings() { return findings; }

        @Override
        public void visit(VariableDeclarationExpr n, Void arg) {
            n.getVariables().forEach(v -> {
                if (v.getType().isArrayType() && v.getInitializer().isPresent()) {
                    Expression init = v.getInitializer().get();
                    if (init.isArrayCreationExpr()) {
                        ArrayCreationExpr ace = init.asArrayCreationExpr();
                        if (!ace.getLevels().isEmpty()) {
                            ArrayCreationLevel level = ace.getLevels().get(0);
                            if (level.getDimension().isPresent()) {
                                Expression dim = level.getDimension().get();
                                if (dim.isIntegerLiteralExpr()) {
                                    try {
                                        int size = Integer.parseInt(dim.asIntegerLiteralExpr().getValue());
                                        arraySizes.put(v.getNameAsString(), size);
                                    } catch (NumberFormatException ignore) {}
                                }
                            }
                        }
                    } else if (init.isArrayInitializerExpr()) {
                        int size = init.asArrayInitializerExpr().getValues().size();
                        arraySizes.put(v.getNameAsString(), size);
                    }
                }
            });
            super.visit(n, arg);
        }

        @Override
        public void visit(ArrayAccessExpr n, Void arg) {
            Expression name = n.getName();
            Expression index = n.getIndex();
            if (name.isNameExpr() && index.isIntegerLiteralExpr()) {
                String arr = name.asNameExpr().getNameAsString();
                try {
                    int idx = Integer.parseInt(index.asIntegerLiteralExpr().getValue());
                    if (arraySizes.containsKey(arr)) {
                        int size = arraySizes.get(arr);
                        if (idx < 0 || idx >= size) {
                            BugFinding b = new BugFinding();
                            b.setKind("ArrayOutOfBoundsHeuristic");
                            b.setMessage(String.format("Array '%s' accessed with literal index %d but known size is %d", arr, idx, size));
                            b.setLine(n.getBegin().map(p -> p.line).orElse(-1));
                            findings.add(b);
                        }
                    } else {
                        // unknown size; can't say much
                    }
                } catch (NumberFormatException ignore) {}
            }
            super.visit(n, arg);
        }
    }
}
