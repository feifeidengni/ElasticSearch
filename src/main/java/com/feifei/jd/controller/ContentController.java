package com.feifei.jd.controller;

import com.feifei.jd.service.ContentService;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//前端的请求编写
@RestController
public class ContentController
{

    @Autowired
    private ContentService contentService;
    // 根据条件查询
    @GetMapping("/parse/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword) throws IOException {
        return this.contentService.parseContent(keyword);
    }

    // 根据分页查询
    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> searchPage(@PathVariable("keyword") String keyword,
                                                @PathVariable("pageNo") int pageNo,
                                                @PathVariable("pageSize") int pageSize) throws IOException {
        List<Map<String, Object>> maps = contentService.searchHighlightBuilder(keyword, pageNo, pageSize);
        return maps;
    }


}
