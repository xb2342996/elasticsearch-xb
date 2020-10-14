package com.xxbb.elasticsearch.entities;

import com.xxbb.springframework.data.elasticsearch.annotations.DateFormat;
import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(indexName = "test-person")
public class Person implements Serializable {
    @Id
    private Long id;
    private String name;
    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
