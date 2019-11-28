package com.wise.demo.services.elasticsearch.model;

import com.wise.demo.services.elasticsearch.Constants;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.List;

@Document(indexName = Constants.EMPLOYEE_ALIAS, type = Constants.INDEX_TYPE, createIndex = false)
@Setter
@Getter
@ToString
public class Employee implements Serializable {

    private static final long serialVersionUID = -3906528329018922517L;

    @Id
    private Long id;

    @Field(type = FieldType.Object)
    private Organization organization;

    @Field(type = FieldType.Object)
    private List<Department> department;

    private String name;

    private int age;

    private String position;

}
