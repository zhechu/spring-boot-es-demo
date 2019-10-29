package com.wise.demo.services.elasticsearch.model;

import com.wise.demo.services.elasticsearch.Constants;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.Date;

@Document(indexName = Constants.TB_VIDEO_INDEX, type = Constants.INDEX_DOC_TYPE)
@Setter
@Getter
@ToString
public class TbVideo implements Serializable {

    private static final long serialVersionUID = 5280843612227874579L;

    @Id
    private Long id;

    private String videoTitle;

    private Long score;

    private Date createTime;

    private Date lastUpdateTime;

}
