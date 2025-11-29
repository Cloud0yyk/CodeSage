package com.cloud.code_sage_analyzer.service;

import com.cloud.code_sage_analyzer.pojo.dto.AnalysisRequest;
import com.cloud.code_sage_analyzer.pojo.vo.AnalysisResult;


public interface AnalysisService {
    /**
     * 分析源代码
     * @param req 源代码
     * @return
     */
    public AnalysisResult analyze(AnalysisRequest req);
}
