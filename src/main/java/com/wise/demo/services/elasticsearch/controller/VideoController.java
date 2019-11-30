package com.wise.demo.services.elasticsearch.controller;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.google.common.collect.Sets;
import com.wise.demo.services.elasticsearch.model.Video;
import com.wise.demo.services.elasticsearch.repository.VideoRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.RandomScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    ElasticsearchTemplate template;

    /**
     * 模糊查询，按视频 score 降序排序
     * 如：http://localhost:9901/video/title/Designer?page=1&size=10
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
     * 如：http://localhost:9901/video/address/match?address=石巷&page=1&size=10
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
     * 如：http://localhost:9901/video/address/pinyin?address=kela玛依&page=1&size=10
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
     * 如：http://localhost:9901/video/address/composite?address=shi巷&page=1&size=100
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
     * 如：http://localhost:9901/video/address/seed?seed=10000&page=1&size=100
     * @param seed
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/address/seed")
    public List<Video> findByAddressWithSeed(
            @RequestParam Long seed,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        /*
        POST /video/_search
        {
          "query": {
            "function_score": {
              "random_score": {
                "seed": 100
              }
            }
          }
        }
        */
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.functionScoreQuery(new RandomScoreFunctionBuilder().seed(seed)))
                // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
                .withPageable(PageRequest.of(page - 1, size))
                .build();

        return template.queryForList(searchQuery, Video.class);
    }

    /**
     * 搜索建议（分词）
     * 如：http://localhost:9901/video/title/suggest/term?title=factor
     * @param title
     * @return
     */
    @GetMapping("/title/suggest/term")
    public Set<String> suggestTerm(@RequestParam String title) {
        Set<String> titleSuggestSet = Sets.newHashSet();

        /*
        POST /video/_search
        {
          "suggest": {
            "titleSuggestion": {
              "text": "factor",
              "term": {
                "suggest_mode": "popular",
                "field": "title",
                "prefix_length":0
              }
            }
          }
        }
        */
        String suggestionName = "titleSuggestion";
        SuggestBuilder suggestBuilder = new SuggestBuilder()
                .addSuggestion(
                        suggestionName,
                        SuggestBuilders.termSuggestion("title")
                                .text(title)
                                .suggestMode(TermSuggestionBuilder.SuggestMode.POPULAR)
                                .prefixLength(0));
        SearchResponse searchResponse = template.suggest(suggestBuilder, Video.class);
        Suggest suggest = searchResponse.getSuggest();

        titleSuggestSet.addAll(getSuggestSet(suggest, suggestionName));

        return titleSuggestSet;
    }

    /**
     * 搜索建议（短语）
     * 如：http://localhost:9901/video/title/suggest/phrase?title=factor
     * @param title
     * @return
     */
    @GetMapping("/title/suggest/phrase")
    public Set<String> suggestPhrase(@RequestParam String title) {
        Set<String> titleSuggestSet = Sets.newHashSet();

        /*
        POST /video/_search
        {
          "suggest": {
            "titleSuggestion": {
              "text": "factor",
              "phrase": {
                "field": "title",
                "max_errors": 2,
                "confidence": 0
              }
            }
          }
        }
        */
        String suggestionName = "titleSuggestion";
        SuggestBuilder suggestBuilder = new SuggestBuilder()
                .addSuggestion(
                        suggestionName,
                        SuggestBuilders.phraseSuggestion("title")
                                .text(title)
                                .maxErrors(2)
                                .confidence(0));
        SearchResponse searchResponse = template.suggest(suggestBuilder, Video.class);
        Suggest suggest = searchResponse.getSuggest();

        titleSuggestSet.addAll(getSuggestSet(suggest, suggestionName));

        return titleSuggestSet;
    }

    /**
     * 搜索建议（分词和短语组合）
     * 如：http://localhost:9901/video/title/suggest/composite?title=factor
     * @param title
     * @return
     */
    @GetMapping("/title/suggest/composite")
    public Set<String> suggestComposite(@RequestParam String title) {
        Set<String> titleSuggestSet = Sets.newHashSet();

        /*
        POST /video/_search
        {
          "suggest": {
            "titleTermSuggestion": {
              "text": "factor",
              "term": {
                "suggest_mode": "popular",
                "field": "title",
                "prefix_length":0
              }
            },
            "titlePhraseSuggestion": {
              "text": "factor",
              "phrase": {
                "field": "title",
                "max_errors": 2,
                "confidence": 0
              }
            }
          }
        }
        */

        String suggestionTremName = "titleTermSuggestion";
        String suggestionPhraseName = "titlePhraseSuggestion";
        SuggestBuilder suggestBuilder = new SuggestBuilder()
                .addSuggestion(
                        suggestionTremName,
                        SuggestBuilders.termSuggestion("title")
                                .text(title)
                                .suggestMode(TermSuggestionBuilder.SuggestMode.POPULAR)
                                .prefixLength(0))
                .addSuggestion(
                        suggestionPhraseName,
                        SuggestBuilders.phraseSuggestion("title")
                                .text(title)
                                .maxErrors(2)
                                .confidence(0));
        SearchResponse searchResponse = template.suggest(suggestBuilder, Video.class);
        Suggest suggest = searchResponse.getSuggest();

        titleSuggestSet.addAll(getSuggestSet(suggest, suggestionTremName));
        titleSuggestSet.addAll(getSuggestSet(suggest, suggestionPhraseName));

        return titleSuggestSet;
    }

    /**
     * 获取建议
     *
     * @param suggest
     * @param suggestionTremName
     * @return java.util.Set<java.lang.String>
     * @author lingyuwang
     * @date 2019/11/29 19:46
     */
    private Set<String> getSuggestSet(Suggest suggest, String suggestionTremName) {
        Set<String> titleSuggestSet = Sets.newHashSet();

        Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion = suggest.getSuggestion(suggestionTremName);
        for (Suggest.Suggestion.Entry entry : suggestion.getEntries()) {
            List<Suggest.Suggestion.Entry.Option> options = entry.getOptions();
            for (Suggest.Suggestion.Entry.Option option : options) {
                Text text = option.getText();
                if (text != null) {
                    titleSuggestSet.add(text.string());
                }
            }
        }

        return titleSuggestSet;
    }

}
