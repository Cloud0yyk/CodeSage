package com.cloud.codesageBackend.pojo.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AnalysisResult {
    /**
     * overall metrics
     */
    private Map<String, Integer> metrics; // e.g. LOC, total_cc, max_nesting

    /**
     * per-method / per-node details
     */
    private List<MetricDetail> methodMetrics;

    /**
     * bug findings
     */
    private List<BugFinding> findings;

    /**
     * def-use info
     */
    private List<DefUseInfo> defUseInfos;
}
