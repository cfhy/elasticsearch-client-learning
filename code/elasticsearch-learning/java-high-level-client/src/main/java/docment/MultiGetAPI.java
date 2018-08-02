package docment;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.Map;

public class MultiGetAPI {
    public static void main(String[] args) throws IOException {
        //高级客户端提供了批量处理器以协助批量请求
        //BulkRequest可以在一次请求中执行多个索引，更新或者删除操作。一次请求至少得有一个操作。
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")));
        MultiGetRequest request = new MultiGetRequest();
        request.add(new MultiGetRequest.Item(
                "index", // Index
                "type",  // Type
                "example_id")); //Document id
        //再添加一个
        request.add(new MultiGetRequest.Item("index", "type", "another_id"));
        //===============================可选参数start====================================
        //multiGet支持和get API支持的相同可选参数。 您可以在每一项上设置这些可选参数：
        //禁止获取source，默认为启用
        request.add(new MultiGetRequest.Item("index", "type", "example_id")
                .fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE));

        //配置source包含指定的字段：
        String[] includes = new String[] {"foo", "*r"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        request.add(new MultiGetRequest.Item("index", "type", "example_id")
                .fetchSourceContext(fetchSourceContext));

        //配置source排除指定的字段：
        String[] includes1 = Strings.EMPTY_ARRAY;
        String[] excludes1 = new String[] {"foo", "*r"};
        FetchSourceContext fetchSourceContext1 =
                new FetchSourceContext(true, includes, excludes);
        request.add(new MultiGetRequest.Item("index", "type", "example_id")
                .fetchSourceContext(fetchSourceContext1));

        //配置获取指定的stored字段，要求字段在mappings中是分开存储的（stored存储的原始字段，未经过分词的）
        request.add(new MultiGetRequest.Item("index", "type", "example_id")
                .storedFields("foo"));
        MultiGetResponse response = client.multiGet(request);
        MultiGetItemResponse item = response.getResponses()[0];
        //获取foo的stored字段，要求字段和mappings中是分开存储的
        String value = item.getResponse().getField("foo").getValue();

        // Routing value
        request.add(new MultiGetRequest.Item("index", "type", "with_routing")
                .routing("some_routing"));
        // Parent value
        request.add(new MultiGetRequest.Item("index", "type", "with_parent")
                .parent("some_parent"));
        request.add(new MultiGetRequest.Item("index", "type", "with_version")
                .versionType(VersionType.EXTERNAL)// Version type
                .version(10123L));//Version

        //preference, realtime 和 refresh可以设置在主请求上，而不能设置在每项上
        request.preference("some_preference");//preference值
        request.realtime(false);//将realtime标志设置为false（默认为true）
        request.refresh(true);//在检索文档之前执行刷新（默认为false）


        //===============================同步执行====================================
        //构建MultiGetRequest后，您可以用multiGet来同步执行：
        MultiGetResponse response1 = client.multiGet(request);

        //===============================异步执行====================================
        //异步方法不会阻塞并会立即返回。完成后，如果执行成功完成，则使用onResponse方法回调ActionListener，如果失败则使用onFailure方法。
        //MultiGetResponse  的典型监听器如下所示：
        ActionListener<MultiGetResponse> listener = new ActionListener<MultiGetResponse>() {
            @Override
            public void onResponse(MultiGetResponse response) {
                //执行成功完成时调用。  response以参数的形式提供。
            }

            @Override
            public void onFailure(Exception e) {
                // 在失败的情况下调用。 引发的异常作为参数提供。
            }
        };

        //Multi Get Response
        MultiGetItemResponse firstItem = response.getResponses()[0];
        //assertNull(firstItem.getFailure());//getFailure返回null，由于这没有失败。
        GetResponse firstGet = firstItem.getResponse();//getResponse返回GetResponse。
        String index = firstItem.getIndex();
        String type = firstItem.getType();
        String id = firstItem.getId();
        if (firstGet.isExists()) {
            long version = firstGet.getVersion();
            String sourceAsString = firstGet.getSourceAsString();//以字符串形式获取文档
            Map<String, Object> sourceAsMap = firstGet.getSourceAsMap();//以Map <String，Object>形式获取文档
            byte[] sourceAsBytes = firstGet.getSourceAsBytes();//以字节数组的形式检索文档
        } else {
            //处理未找到文档的方案。 请注意，虽然返回的响应具有404状态代码，
            // 但仍返回有效的GetResponse而不是抛出异常。
            // 此类响应不持有任何源文档，并且其isExists方法返回false。

            //当其中一个子请求执行不存在的索引时，getFailure将包含异常：
            MultiGetItemResponse missingIndexItem = response.getResponses()[0];
            //assertNull(missingIndexItem.getResponse());//getResponse 是 null.
            //getFailure不为null，并且包含Exception,该异常实际上是一个ElasticsearchException
            Exception e = missingIndexItem.getFailure().getFailure();
            ElasticsearchException ee = (ElasticsearchException) e;
            // TODO status is broken! fix in a followup
            //它的状态为NOT_FOUND。 要不是这是一个multi get，它就是一个HTTP 404。
            // assertEquals(RestStatus.NOT_FOUND, ee.status());
            //getMessage解释了实际原因，没有这样的索引。
            //assertThat(e.getMessage(),containsString("reason=no such index"));
        }

        //如果请求特定文档版本，并且现有文档具有不同的版本号，则会引发版本冲突：
        MultiGetRequest request2 = new MultiGetRequest();
        request2.add(new MultiGetRequest.Item("index", "type", "example_id")
                .version(1000L));
        MultiGetResponse response2 = client.multiGet(request2);
        MultiGetItemResponse item2 = response.getResponses()[0];
        //assertNull(item.getResponse());//getResponse 是 null.
        //getFailure不为null，并且包含Exception,该异常实际上是一个ElasticsearchException
        Exception e = item.getFailure().getFailure();
        ElasticsearchException ee = (ElasticsearchException) e;
        // TODO status is broken! fix in a followup
        //此时它的状态为NOT_FOUND。 要不是这是一个multi get，它就是一个HTTP 409 。
        // assertEquals(RestStatus.CONFLICT, ee.status());
        //getMessage解释了实际原因，即版本冲突。
        //assertThat(e.getMessage(),
        //containsString("version conflict, current version [1] is "
        //                + "different than the one provided [1000]"));
    }
}
