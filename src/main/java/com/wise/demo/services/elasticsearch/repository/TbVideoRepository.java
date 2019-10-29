package com.wise.demo.services.elasticsearch.repository;

import com.wise.demo.services.elasticsearch.model.TbVideo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TbVideoRepository extends ElasticsearchCrudRepository<TbVideo, Long> {

    List<TbVideo> findByVideoTitleLike(String videoTitle, Pageable pageable);

}
