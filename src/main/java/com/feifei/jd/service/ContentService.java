package com.feifei.jd.service;

import com.alibaba.fastjson.JSON;
import com.feifei.jd.pojo.Content;
import com.feifei.jd.util.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//业务编写
@Service
public class ContentService
{
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    // 1、解析数据 放入到 es中
    public Boolean parseContent(String keyword) throws IOException {
        //获取数据集合
        List<Content> contents = new HtmlParseUtil().parseJD(keyword);
        // 吧数据放入 es中
        // 获取请求对象
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");  //设置超时时间  2分钟
        for (int i = 0; i < contents.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("jd_goods")
                    .source(JSON.toJSONString(contents.get(i)), XContentType.JSON)
            );
        }
        BulkResponse bulkResponse = this.restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        //  执行成功则 返回 true
        return !bulkResponse.hasFailures();
    }

    // 2.获取数据实现搜索功能
    public List<Map<String, Object>> searchPage(String keyword,int pageNo,int pagesize) throws IOException {
        //判断分页最小
        if(pageNo<=1){
            pageNo = 1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pagesize);
        //精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        //设置超时时间
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 解析结果存入 map
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            list.add(documentFields.getSourceAsMap());
        }
        return list;
    }

    // 3、获取数据高亮显示
    public List<Map<String, Object>> searchHighlightBuilder(String keyword,int pageNo,int pagesize) throws IOException {
        //判断分页最小
        if(pageNo<=1){
            pageNo = 1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pagesize);

        //精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        //设置超时时间
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");        //设置高亮字段
        highlightBuilder.requireFieldMatch(false);      //是否让多个 字段高亮显示
        highlightBuilder.preTags("<span style='color:red;font-size:16px;' >");           //设置高亮字段的前缀 HTML代码
        highlightBuilder.postTags(" </span> ");
        sourceBuilder.highlighter(highlightBuilder);     //搜索 加入高亮条件

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // 解析结果存入 map
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            // 获取title的数据
            HighlightField title = highlightFields.get("title");
            // 获取原来的结果
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //解析高亮字段，将原来的字段替换为 高亮字段即可~
            if(title != null){
                Text[] fragments = title.fragments();
                String n_title = "";  //定义一个新的字段
                for (Text text : fragments) {
                    n_title+=text;
                }
                sourceAsMap.put("title",n_title); // 高亮字段替换原来的字段即可~
            }
            list.add(sourceAsMap);
        }
        return list;
    }

}
