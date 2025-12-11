package com.cloud.code_sage_model.rag.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import lombok.Data;

@Document(indexName = "code-fix-patterns")
@Data
public class FixPattern {
    @Id
    private String id;

    /**
     * bug类型
     */
    @Field(type = FieldType.Keyword)
    private String bugType;

    /**
     * 编程语言
     */
    @Field(type = FieldType.Keyword)
    private String language;

    /**
     * 描述
     */
    @Field(type = FieldType.Text)
    private String description;

    /**
     * 用于 BM25 检索的文本
     */
    @Field(type = FieldType.Text)
    private String textForBm25;

    /**
     * 修复前代码片段
     */
    @Field(type = FieldType.Text)
    private String codeBefore;

    /**
     * 修复后代码片段
     */
    @Field(type = FieldType.Text)
    private String fixSnippet;

    /**
     * 向量嵌入（768 维）
     */
    @Field(type = FieldType.Dense_Vector, dims = 768)
    private float[] embedding;

    /**
     * 置信度（自动添加的为 0.6，人工为 1.0）
     */
    @Field(type = FieldType.Float)
    private Float confidence = 1.0f;

}