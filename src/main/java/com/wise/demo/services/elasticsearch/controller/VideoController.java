package com.wise.demo.services.elasticsearch.controller;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.wise.demo.services.elasticsearch.model.Video;
import com.wise.demo.services.elasticsearch.repository.VideoRepository;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.RandomScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
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
     * 全文检索，按 _score 降序排序（兼容繁体字检索，实现：在应用层将繁体字转为简体字）
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

        // 繁体字转为简体字
        address = ZhConverterUtil.convertToSimple(address);

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("address", address))
                // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
                .withPageable(PageRequest.of(page - 1, size)).build();

        return template.queryForList(searchQuery, Video.class);
    }

    /**
     * 全文按拼音检索，按 _score 降序排序
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

    /**
     * 复合检索（整合IK分词检索和拼音检索），按 _score 降序排序（新的算分 = ⽼的算分 * log( 1 + factor *投票数 )）
     * 如：http://localhost:8080/video/address/composite?address=shi巷&page=1&size=100
     * @param address
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/address/composite")
    public List<Video> findByAddressWithComposite(
            @RequestParam String address,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 繁体字转为简体字
        address = ZhConverterUtil.convertToSimple(address);

        /*
        POST /video/_search
        {
            "query":{
                "function_score":{
                    "query":{
                        "bool":{
                           "must": {
                              "match": {
                                "address": {
                                  "query": "江",
                                  "boost": 2
                                }
                              }
                           },
                           "should": {
                              "match_phrase": {
                                "address.pinyin": {
                                  "query": "江",
                                  "boost": 0.1
                                }
                              }
                           }
                        }
                    }
                }
            }
        }
        */
//        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.functionScoreQuery(
//                        QueryBuilders.boolQuery()
//                                .must(QueryBuilders.matchQuery("address",address)).boost(2)
//                                .should(QueryBuilders.matchPhraseQuery("address.pinyin", address).boost(0.1F))))
//                // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
//                .withPageable(PageRequest.of(page - 1, size))
//                .build();


        /*
        POST /video/_search
        {
            "query":{
                "function_score":{
                    "query":{
                        "bool":{
                           "must": {
                              "match": {
                                "address": {
                                  "query": "江",
                                  "boost": 2
                                }
                              }
                           },
                           "should": {
                              "match_phrase": {
                                "address.pinyin": {
                                  "query": "江",
                                  "boost": 0.1
                                }
                              }
                           }
                        }
                    },
                    "field_value_factor": {
                      "field": "score",
                      "modifier": "log1p" ,
                      "factor": 0.1
                    },
                    "boost_mode": "multiply",
                    "max_boost": 3
                }
            }
        }
        */
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.functionScoreQuery(
                        QueryBuilders.boolQuery()
                                .must(QueryBuilders.matchQuery("address",address)).boost(2)
                                .should(QueryBuilders.matchPhraseQuery("address.pinyin", address).boost(0.1F)),
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        ScoreFunctionBuilders.fieldValueFactorFunction("score")
                                        .modifier(FieldValueFactorFunction.Modifier.LOG1P).factor(0.1F)
                                )
                        }).boostMode(CombineFunction.MULTIPLY).maxBoost(3F))
                // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
                .withPageable(PageRequest.of(page - 1, size))
                .build();

        return template.queryForList(searchQuery, Video.class);
    }

    /**
     * 一致性随机检索（提高视频展现率）
     * 如：http://localhost:8080/video/address/seed?seed=10000&page=1&size=100
     * @param seed
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/address/seed")
    public List<Video> findByAddressWithComposite(
            @RequestParam Long seed,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        RandomScoreFunctionBuilder randomScoreFunctionBuilder = new RandomScoreFunctionBuilder();
        randomScoreFunctionBuilder.seed(seed);
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.functionScoreQuery(randomScoreFunctionBuilder))
                // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
                .withPageable(PageRequest.of(page - 1, size))
                .build();

        return template.queryForList(searchQuery, Video.class);
    }

}
