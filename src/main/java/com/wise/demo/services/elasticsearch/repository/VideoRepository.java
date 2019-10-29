package com.wise.demo.services.elasticsearch.repository;

import com.wise.demo.services.elasticsearch.model.Video;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends ElasticsearchCrudRepository<Video, Long> {

    List<Video> findByTitleLike(String title, Pageable pageable);

}
