package com.wise.demo.services.elasticsearch.controller;

import com.wise.demo.services.elasticsearch.model.Video;
import com.wise.demo.services.elasticsearch.repository.VideoRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    ElasticsearchTemplate template;

    /**
     * 模糊查询，按视频 score 降序排序
     * 如：http://localhost:8080/video/title/Designer?page=1&size=10
     * @param title
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/title/{title}")
    public List<Video> findByTitleLike(
            @PathVariable("title") String title,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
        return videoRepository.findByTitleLike(
                title,
                PageRequest.of(page - 1, size, Sort.Direction.DESC, "score"));
    }

    /**
     * 全文检索，按 _score 降序排序
     * 如：http://localhost:8080/video/address/match?address=石巷&page=1&size=10
     * @param address
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/address/match")
    public List<Video> findByAddressWithMatch(
            @RequestParam String address,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("address", address))
                // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
                .withPageable(PageRequest.of(page - 1, size)).build();

        return template.queryForList(searchQuery, Video.class);
    }

    /**
     * 全文按拼音检索（使用场景：查询框联想），按 _score 降序排序
     * 如：http://localhost:8080/video/address/pinyin?address=kela玛依&page=1&size=10
     * @param address
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/address/pinyin")
    public List<Video> findByAddressWithPinyin(
            @RequestParam String address,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchPhraseQuery("address.pinyin", address))
                // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
                .withPageable(PageRequest.of(page - 1, size)).build();

        return template.queryForList(searchQuery, Video.class);
    }

}
