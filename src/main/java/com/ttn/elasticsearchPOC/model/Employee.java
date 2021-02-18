package com.ttn.elasticsearchPOC.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "bluebell", indexStoreType = "employee")
@Data
public class Employee {

    @Id
    private int id;

    private String name;

    private String title;

    private String competency;

    @Field(type = FieldType.Nested, searchAnalyzer = "true")
    private List<String> skills;

    @Field(type = FieldType.Nested, searchAnalyzer = "true")
    private Project currentProject;
}
