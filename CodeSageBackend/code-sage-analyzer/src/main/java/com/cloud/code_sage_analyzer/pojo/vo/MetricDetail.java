package com.cloud.code_sage_analyzer.pojo.vo;

import lombok.Data;

@Data
public class MetricDetail {
    /**
     * 函数名/类名/文件名
     */
    private String name;

    /**
     * 代码行数
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