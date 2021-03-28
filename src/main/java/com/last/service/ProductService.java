package com.last.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.last.pojo.Product;
import com.last.util.HtmlParseUtil;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chenzihao
 * @date 2021/03/26
 */
@Service
public class ProductService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final RestHighLevelClient client;

    public ProductService(@Qualifier("restHighLevelClient") RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * 解析数据放入 es 索引中
     *
     * @param keyword 关键字
     * @return 返回值
     * @throws IOException 抛出异常
     */
    public boolean parseProduct(String keyword) throws IOException {
        List<Product> productList = new HtmlParseUtil().parseJd(keyword);
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        // ArrayList.forEach参数为一个Consumer<T>消费接口
        productList.forEach(product -> {
            try {
                // 批量更新和批量删除，可修改对应的请求
                bulkRequest.add(
                        new IndexRequest("user").source(MAPPER.writeValueAsString(product), XContentType.JSON)
                );
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        // 是否失败，返回false表示成功
        return !bulkResponse.hasFailures();
    }

    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        if (pageNo <= 1) {
            pageNo = 1;
        }
        // 条件搜索
        SearchRequest searchRequest = new SearchRequest("user");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 分页
        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);
        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        // 关闭多个高亮
        highlightBuilder.requireFieldMatch(false);
        searchSourceBuilder.highlighter(highlightBuilder);
        // 精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // 执行搜索
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
            HighlightField title = highlightFieldMap.get("title");
            // 原始的结果
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 将高亮的字段替换
            if (title != null) {
                Text[] fragments = title.fragments();
                StringBuilder nTitle = new StringBuilder();
                for (Text text : fragments) {
                    nTitle.append(text);
                }
                sourceAsMap.put("title", nTitle.toString());
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}
