package com.last;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.last.pojo.User;
import com.last.service.ProductService;
import com.last.util.ElasticSearchConst;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ElasitcsearchApplicationTests {

    private static final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private ProductService service;

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    // ????????????
    @Test
    void contextLoads() throws IOException {
        // ??????????????????
        CreateIndexRequest request = new CreateIndexRequest("user");
        // ???IndicesClient??????????????????????????????
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    // ????????????????????????
    @Test
    void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("test1");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // ????????????
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("test1");
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

    // ????????????
    @Test
    void testAddDocument() throws IOException {
        // ????????????
        User user = new User("?????????", 22);
        // ????????????
        IndexRequest request = new IndexRequest("user");
        // ?????? put /user/_doc/1
        request.id("2");
        request.timeout(TimeValue.timeValueSeconds(1));
        // request.timeout("1s");
        // ?????????????????????????????? Json??????
        request.source(mapper.writeValueAsString(user), XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    // ???????????????????????????????????? GET /index/_doc/1
    @Test
    void testIsExists() throws IOException {
        GetRequest getRequest = new GetRequest("user", "1");
        // ????????? _source ????????????
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // ????????????
    @Test
    void testGetDocument() throws IOException {
        GetRequest getRequest = new GetRequest("user", "1");
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString()); // ?????????????????????
        System.out.println(getResponse); // ?????????????????????????????????
    }

    // ??????????????????
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("user", "1");
        updateRequest.timeout("1s");
        User user = new User("?????????", 14);
        updateRequest.doc(mapper.writeValueAsString(user), XContentType.JSON);
        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    // ????????????
    @Test
    void testDeleteDocument() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("user", "2");
        deleteRequest.timeout("1s");
        DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());
    }

    // ?????????????????????????????????????????????????????????
    @Test
    void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("1s");
        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("user1", 21));
        userList.add(new User("user2", 22));
        userList.add(new User("user3", 23));
        userList.add(new User("user4", 24));
        userList.add(new User("user5", 25));
        userList.add(new User("user6", 26));
        userList.add(new User("user7", 27));
        userList.add(new User("user8", 28));
        userList.add(new User("user9", 29));
        // ArrayList.forEach???????????????Consumer<T>????????????
        userList.forEach(user -> {
            try {
                // ??????????????????????????????????????????????????????
                bulkRequest.add(
                        new IndexRequest("user").source(mapper.writeValueAsString(user), XContentType.JSON)
                );
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.hasFailures()); // ?????????????????????false????????????
    }

    // ??????????????????
    @Test
    void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest(ElasticSearchConst.ES_INDEX);
        // ??????????????????
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // ?????????????????????????????????QueryBuilders???????????????
        // QueryBuilders.termQuery ??????
        // QueryBuilders.matchAllQuery() ????????????
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "user1");
        // MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(5);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(mapper.writeValueAsString(searchResponse.getHits()));
        System.out.println("========================================================");
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsString());
        }
    }

    // ????????????????????????????????????
    @Test
    void testParseProduct() throws IOException {
        boolean is = service.parseProduct("java");
        System.out.println(is);
    }
}
