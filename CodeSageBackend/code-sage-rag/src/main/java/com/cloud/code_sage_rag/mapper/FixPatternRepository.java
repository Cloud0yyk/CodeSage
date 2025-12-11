package com.cloud.code_sage_rag.mapper;

import com.cloud.code_sage_model.rag.entity.FixPattern;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface FixPatternRepository extends ElasticsearchRepository<FixPattern, String> {
}