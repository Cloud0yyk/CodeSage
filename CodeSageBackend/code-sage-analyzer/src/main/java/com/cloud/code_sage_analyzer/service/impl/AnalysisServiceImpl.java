package com.cloud.code_sage_analyzer.service.impl;

import com.cloud.code_sage_analyzer.analyzer.JavaAnalyzer;
import com.cloud.code_sage_analyzer.pojo.dto.AnalysisRequest;
import com.cloud.code_sage_analyzer.pojo.vo.AnalysisResult;
import com.cloud.code_sage_analyzer.service.AnalysisService;
import org.springframework.stereotype.Service;

@Service
public class AnalysisServiceImpl implements AnalysisService {
    private final JavaAnalyzer javaAnalyzer;

    public AnalysisServiceImpl(JavaAnalyzer javaAnalyzer) {
        this.javaAnalyzer = javaAnalyzer;
    }

    public AnalysisResult analyze(AnalysisRequest req) {
        if (req.getLanguage() == null || req.getLanguage().isEmpty() || "java".equalsIgnoreCase(req.getLanguage())) {
            return javaAnalyzer.analyze(req.getCode());
        } else {
            // TODO: integrate ANTLR for other languages or Clang toolchain for C/C++.
            AnalysisResult r = new AnalysisResult();
            r.setFindings(java.util.List.of());
            r.setMethodMetrics(java.util.List.of());
            r.setMetrics(java.util.Map.of("LOC", 0));
            return r;
        }
    }
}
