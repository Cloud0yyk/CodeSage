package com.cloud.codesageBackend.controller;


import com.cloud.codesageBackend.service.AnalysisService;
import com.cloud.codesageBackend.pojo.dto.AnalysisRequest;
import com.cloud.codesageBackend.pojo.vo.AnalysisResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AnalyzeController {

    private final AnalysisService analysisService;

    public AnalyzeController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * 接受 JSON：{ "language": "java", "code": "public class A { ... }" }
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> analyze(@RequestBody AnalysisRequest req) {
        AnalysisResult result = analysisService.analyze(req);
        return ResponseEntity.ok(result);
    }
}