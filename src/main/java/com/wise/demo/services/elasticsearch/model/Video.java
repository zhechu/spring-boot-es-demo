package com.wise.demo.services.elasticsearch.model;

import com.wise.demo.services.elasticsearch.Constants;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.Date;

@Document(indexName = Constants.VIDEO_INDEX, type = Constants.INDEX_TYPE)
@Setter
@Getter
@ToString
public class Video implements Serializable {

    private static final long serialVersionUID = 5280843612227874579L;

    @Id
    private Long id;

    private String title;

    private String address;

    private int score;

    private Date createTime;

}
