package com.cloud.codesageBackend.pojo.dto;

import lombok.Data;

@Data
public class AnalysisRequest {
    /**
     * 编程语言
     */
    private String language;

    /**
     * 源代码
     */
    private String code;
}