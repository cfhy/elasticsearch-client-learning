package docment;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @description:Index API
 * @author: yyb
 * @create: 2018/4/10.
 */
public class IndexAPI {
   public static void main(String[]args) throws IOException {
       RestHighLevelClient client = new RestHighLevelClient(
               RestClient.builder(
                       new HttpHost("localhost", 9200, "http"),
                       new HttpHost("localhost", 9201, "http")));
       IndexRequest indexRequest1 = new IndexRequest(
               "posts",//索引名称
               "doc",//类型名称
               "1");//文档ID

       //==============================提供文档源========================================
       //方式1：以字符串形式提供
       String jsonString = "{" +
               "\"user\":\"kimchy\"," +
               "\"postDate\":\"2013-01-30\"," +
               "\"message\":\"trying out Elasticsearch\"" +
               "}";
       indexRequest1.source(jsonString, XContentType.JSON);

       //方式2：以Map形式提供
       Map<String, Object> jsonMap = new HashMap<>();
       jsonMap.put("user", "kimchy");
       jsonMap.put("postDate", new Date());
       jsonMap.put("message", "trying out Elasticsearch");
       //Map会自动转换为JSON格式的文档源
       IndexRequest indexRequest2 = new IndexRequest("posts", "doc", "1")
               .source(jsonMap);

       // 方式3：文档源以XContentBuilder对象的形式提供，Elasticsearch内部会帮我们生成JSON内容

       XContentBuilder builder = XContentFactory.jsonBuilder();
       builder.startObject();
       {
           builder.field("user", "kimchy");
           builder.field("postDate", new Date());
           builder.field("message", "trying out Elasticsearch");
       }
       builder.endObject();
       IndexRequest indexRequest3 = new IndexRequest("posts", "doc", "1")
               .source(builder);

       //方式4：以Object key-pairs提供的文档源，它会被转换为JSON格式
       IndexRequest indexRequest4 = new IndexRequest("posts", "doc", "1")
        .source("user", "kimchy",
               "postDate", new Date(),
               "message", "trying out Elasticsearch");

       //===============================可选参数start====================================
       indexRequest1.routing("routing");//设置路由值
       indexRequest1.parent("parent");//设置parent值

       //设置超时：等待主分片变得可用的时间
       indexRequest1.timeout(TimeValue.timeValueSeconds(1));//TimeValue方式
       indexRequest1.timeout("1s");//字符串方式

       //刷新策略
       indexRequest1.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);//WriteRequest.RefreshPolicy实例方式
       indexRequest1.setRefreshPolicy("wait_for");//字符串方式

       indexRequest1.version(2);//设置版本

       indexRequest1.versionType(VersionType.EXTERNAL);//设置版本类型

       //操作类型
       indexRequest1.opType(DocWriteRequest.OpType.CREATE);//DocWriteRequest.OpType方式
       indexRequest1.opType("create");//字符串方式, 可以是 create 或 update (默认)

       //The name of the ingest pipeline to be executed before indexing the document
       indexRequest1.setPipeline("pipeline");

       //===============================执行====================================
       //同步执行
       IndexResponse indexResponse = client.index(indexRequest1);

       //异步执行
       //IndexResponse 的典型监听器如下所示：
       //异步方法不会阻塞并立即返回。
       ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
           @Override
           public void onResponse(IndexResponse indexResponse) {
                //执行成功时调用。 Response以参数方式提供
           }

           @Override
           public void onFailure(Exception e) {
               //在失败的情况下调用。 引发的异常以参数方式提供
           }
       };
       //异步执行索引请求需要将IndexRequest实例和ActionListener实例传递给异步方法：
       client.indexAsync(indexRequest2, listener);

       //Index Response
       //返回的IndexResponse允许检索有关执行操作的信息，如下所示：
       String index = indexResponse.getIndex();
       String type = indexResponse.getType();
       String id = indexResponse.getId();
       long version = indexResponse.getVersion();
       if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            //处理（如果需要）第一次创建文档的情况
       } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            //处理（如果需要）文档被重写的情况
       }
       ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
       if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            //处理成功分片数量少于总分片数量的情况
       }
       if (shardInfo.getFailed() > 0) {
           for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
               String reason = failure.reason();//处理潜在的失败
           }
       }

       //如果存在版本冲突，则会抛出ElasticsearchException：
       IndexRequest request = new IndexRequest("posts", "doc", "1")
               .source("field", "value")
               .version(1);
       try {
           IndexResponse response = client.index(request);
       } catch(ElasticsearchException e) {
           if (e.status() == RestStatus.CONFLICT) {
                //引发的异常表示返回了版本冲突错误
           }
       }

       //如果opType设置为创建但是具有相同索引，类型和ID的文档已存在，则也会发生同样的情况：
       request = new IndexRequest("posts", "doc", "1")
               .source("field", "value")
               .opType(DocWriteRequest.OpType.CREATE);
       try {
           IndexResponse response = client.index(request);
       } catch(ElasticsearchException e) {
           if (e.status() == RestStatus.CONFLICT) {
                //引发的异常表示返回了版本冲突错误
           }
       }
   }
}


