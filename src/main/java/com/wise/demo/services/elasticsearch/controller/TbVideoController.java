package com.wise.demo.services.elasticsearch.controller;

import com.wise.demo.services.elasticsearch.model.TbVideo;
import com.wise.demo.services.elasticsearch.repository.TbVideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tb_video")
public class TbVideoController {

    @Autowired
    TbVideoRepository tbVideoRepository;

    /**
     * 模糊查询，按视频 score 降序排序
     * 如：http://localhost:8080/tb_video/videoTitle?videoTitle=音乐
     * @param videoTitle
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/videoTitle")
    public List<TbVideo> findByTitleLike(
            @RequestParam String videoTitle,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
        return tbVideoRepository.findByVideoTitleLike(
                videoTitle,
                PageRequest.of(page - 1, size, Sort.Direction.DESC, "score"));
    }

    /**
     * 根据ID列表检索
     * 如：http://localhost:8080/tb_video/ids [129,136]
     * @param ids
     * @return
     */
    @PostMapping("/ids")
    public List<TbVideo> findByIdIs(@RequestBody List<Long> ids) {
        // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
        return tbVideoRepository.findByIdIn(ids);
    }

}
