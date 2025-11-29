package com.cloud.code_sage_analyzer.service;

import com.cloud.code_sage_model.analyzer.dto.AnalysisRequest;
import com.cloud.code_sage_model.analyzer.vo.AnalysisResult;


public interface AnalysisService {
    /**
     * 分析源代码
     * @param req 源代码
     * @return
     */
    public AnalysisResult analyze(AnalysisRequest req);
}
