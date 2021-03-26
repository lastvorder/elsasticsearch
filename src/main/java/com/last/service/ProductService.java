package com.last.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.last.pojo.Product;
import com.last.util.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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
        bulkRequest.timeout("1s");
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
}
