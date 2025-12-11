package com.cloud.code_sage_rag.service;

import java.util.List;
import com.cloud.code_sage_model.rag.entity.FixPattern;


public interface RagService {
    /**
     * 根据编程语言和代码找回对应的修复模式
     * @param query
     * @param language
     * @param topK
     * @return
     */
    public List<FixPattern> retrievePatterns(String query, String language, int topK);
}
