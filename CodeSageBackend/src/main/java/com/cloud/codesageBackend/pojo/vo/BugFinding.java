package com.cloud.codesageBackend.pojo.vo;

import lombok.Data;

@Data
public class BugFinding {
    /**
     * bug类别，例如
     * "ArrayOutOfBoundsHeuristic": 数组越界
     * "UninitializedVariable": 未初始化
     * "InfiniteLoop": 无限循环
     * "EmptyCatch": 空catch块
     */
    private String kind;

    /**
     * bug信息
     */
    private String message;

    /**
     * bug所在行
     */
    private int line;
}
