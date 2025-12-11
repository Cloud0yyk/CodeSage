package com.cloud.code_sage_rag.service.impl;

import co.elastic.clients.elasticsearch._types.KnnSearch;
import com.cloud.cloud_sage_common.common.ErrorCode;
import com.cloud.cloud_sage_common.exception.BusinessException;
import com.cloud.code_sage_rag.service.RagService;
import com.cloud.code_sage_model.rag.entity.FixPattern;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RagServiceImpl implements RagService {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private EmbeddingModel embeddingModel;


    // 混合检索（BM25 + Dense）
    @Override
    public List<FixPattern> retrievePatterns(String query, String language, int topK) {
        try {

            /** Step 1: 生成查询 embedding */
            List<Double> embed = embeddingModel.embed(query).embedding();
            List<Float> vector = embed.stream().map(Double::floatValue).toList();

            /** Step 2: 分别执行 BM25 与 kNN */
            // ---- BM25 Query ----
            Query bm25Query = MatchQuery.of(m -> m.field("textForBm25").query(query))._toQuery();

            SearchResponse<FixPattern> bm25Resp = elasticsearchClient.search(
                    s -> s.index("code-fix-patterns")
                            .query(bm25Query)
                            .size(topK * 2),
                    FixPattern.class
            );

            // ---- kNN Query ----
            KnnSearch knnSearch = KnnSearch.of(k -> k
                    .field("embedding")    // dense_vector 字段
                    .queryVector(vector)        // 查询向量 List<Float>
                    .k(topK * 2)
                    .numCandidates(topK * 4)
            );

            // ---- 执行 kNN 搜索 ----
            SearchResponse<FixPattern> knnResp = elasticsearchClient.search(
                    s -> s.index("code-fix-patterns")
                            .knn(List.of(knnSearch))
                            .size(topK * 2),
                    FixPattern.class
            );

            /** Step 3: RRF 融合 */
            Map<String, Double> scores = new HashMap<>();
            int k = 60;

            // BM25 rank
            for (int i = 0; i < bm25Resp.hits().hits().size(); i++) {
                Hit<FixPattern> hit = bm25Resp.hits().hits().get(i);
                scores.merge(hit.id(), 1.0 / (k + i + 1), Double::sum);
            }

            // kNN rank
            for (int i = 0; i < knnResp.hits().hits().size(); i++) {
                Hit<FixPattern> hit = knnResp.hits().hits().get(i);
                scores.merge(hit.id(), 1.0 / (k + i + 1), Double::sum);
            }

            /** Step 4: 按 RRF 分数排序，取前 topK */
            List<String> topIds = scores.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(topK)
                    .map(Map.Entry::getKey)
                    .toList();

            /** Step 5: 按最终排序顺序返回 FixPattern */
            Map<String, FixPattern> docMap = new HashMap<>();

            // 合并 bm25 与 knn 结果
            for (Hit<FixPattern> hit : bm25Resp.hits().hits()) {
                FixPattern fp = hit.source();
                if (fp != null) fp.setId(hit.id());
                docMap.put(hit.id(), fp);
            }
            for (Hit<FixPattern> hit : knnResp.hits().hits()) {
                FixPattern fp = hit.source();
                if (fp != null) fp.setId(hit.id());
                docMap.put(hit.id(), fp);
            }

            // 按 RRF 顺序输出
            List<FixPattern> finalList = new ArrayList<>();
            for (String id : topIds) {
                if (docMap.containsKey(id)) {
                    finalList.add(docMap.get(id));
                }
            }

            return finalList;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "RAG service error!");
        }
    }

}
