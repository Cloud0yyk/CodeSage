package com.cloud.codesageBackend.service;

import com.cloud.codesageBackend.pojo.dto.AnalysisRequest;
import com.cloud.codesageBackend.pojo.vo.AnalysisResult;
import com.cloud.codesageBackend.analyzer.JavaAnalyzer;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

    private final JavaAnalyzer javaAnalyzer;

    public AnalysisService(JavaAnalyzer javaAnalyzer) {
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
