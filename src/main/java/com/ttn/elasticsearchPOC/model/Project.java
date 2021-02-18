package com.ttn.elasticsearchPOC.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

@Data
public class Project implements Serializable {
    @Id
    private String projectId;
    @Field(type = FieldType.Keyword)
    private String projectName;
    @Field(type = FieldType.Keyword)
    private String clientName;
}
