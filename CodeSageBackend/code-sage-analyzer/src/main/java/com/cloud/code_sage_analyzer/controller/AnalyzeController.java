package com.cloud.code_sage_analyzer.controller;


import com.cloud.cloud_sage_common.common.BaseResponse;
import com.cloud.cloud_sage_common.common.ResultUtils;
import com.cloud.code_sage_analyzer.service.AnalysisService;
import com.cloud.code_sage_analyzer.pojo.dto.AnalysisRequest;
import com.cloud.code_sage_analyzer.pojo.vo.AnalysisResult;
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
    public BaseResponse<AnalysisResult> analyze(@RequestBody AnalysisRequest req) {
        AnalysisResult result = analysisService.analyze(req);
        return ResultUtils.success(result);
    }
}