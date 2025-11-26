package com.cloud.codesageBackend.pojo.dto;

import lombok.Data;

@Data
public class MetricDetail {
    /**
     *
     */
    private String name;

    /**
     *
     */
    private int loc;

    /**
     * 圈复杂度
     */
    private int cyclomaticComplexity;

    /**
     * 最大嵌套深度
     */
    private int maxNestingDepth;
}