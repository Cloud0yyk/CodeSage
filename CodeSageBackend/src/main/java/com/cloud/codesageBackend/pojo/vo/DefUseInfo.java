package com.cloud.codesageBackend.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class DefUseInfo {
    /**
     * 函数名/类名/文件名
     */
    private String methodName;

    /**
     * 变量
     */
    private String variable;

    /**
     * 定义行
     */
    private List<Integer> defLines;

    /**
     * 使用行
     */
    private List<Integer> useLines;

    /**
     * 是否使用
     */
    private boolean used; // convenience
}