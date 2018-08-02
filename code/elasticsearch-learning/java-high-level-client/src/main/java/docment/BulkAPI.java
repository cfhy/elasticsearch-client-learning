package docment;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

/**
 * @description:Get API
 * @author: yyb
 * @create: 2018/4/10.
 */
public class BulkAPI {
    public static void main(String[] args) throws IOException {
        //高级客户端提供了批量处理器以协助批量请求
        //BulkRequest可以在一次请求中执行多个索引，更新或者删除操作。一次请求至少得有一个操作。
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")));
        //创建BulkRequest实例
        BulkRequest request = new BulkRequest();
        //使用IndexRequest添加三个文档，不清楚用法可以参考IndexAPI
        request.add(new IndexRequest("posts", "doc", "1")
                .source(XContentType.JSON, "field", "foo"));
        request.add(new IndexRequest("posts", "doc", "2")
                .source(XContentType.JSON, "field", "bar"));
        request.add(new IndexRequest("posts", "doc", "3")
                .source(XContentType.JSON, "field", "baz"));
        //同一个BulkRequest可以添加不同类型的操作

        // 添加 DeleteRequest到BulkRequest，不清楚用法可以参考Delete API
        request.add(new DeleteRequest("posts", "doc", "3"));
        // 添加 UpdateRequest到BulkRequest，不清楚用法可以参考Update API
        request.add(new UpdateRequest("posts", "doc", "2")
                .doc(XContentType.JSON, "other", "test"));
        // 添加 一个使用SMILE格式的IndexRequest
        request.add(new IndexRequest("posts", "doc", "4")
                .source(XContentType.SMILE, "field", "baz"));

        //===============================可选参数start====================================
        //设置超时，等待批处理被执行的超时时间（使用TimeValue形式）
        request.timeout(TimeValue.timeValueMinutes(2));
        //设置超时，等待批处理被执行的超时时间（字符串形式）
        request.timeout("2m");

        //刷新策略
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);//WriteRequest.RefreshPolicy实例方式
        request.setRefreshPolicy("wait_for");//字符串方式

        //设置在执行索引/更新/删除操作之前必须处于活动状态的分片副本数。
        request.waitForActiveShards(2);
        //使用ActiveShardCount方式来提供分片副本数：
        // 可以是ActiveShardCount.ALL，ActiveShardCount.ONE
        // 或ActiveShardCount.DEFAULT（默认）
        request.waitForActiveShards(ActiveShardCount.ALL);

        //===============================同步执行====================================
        BulkResponse bulkResponse = client.bulk(request);

        //===============================异步执行====================================
        //异步方法不会阻塞并会立即返回。完成后，如果执行成功完成，则使用onResponse方法回调ActionListener，如果失败则使用onFailure方法。
        //BulkResponse  的典型监听器如下所示：
        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkResponse) {
                //执行成功完成时调用。 response作为参数提供，并包含已执行的每个操作的单个结果列表。 请注意，一个或多个操作可能已失败，然而其他操作已成功执行。
            }

            @Override
            public void onFailure(Exception e) {
                //在整个BulkRequest失败时调用。 在这种情况下，exception作为参数提供，并且没有执行任何操作。
            }
        };
        //批量请求的异步执行需要将BulkRequest实例和ActionListener实例传递给异步方法：
        //当BulkRequest执行完成时，ActionListener会被调用
        client.bulkAsync(request, listener);

        //返回的BulkResponse包含有关已执行操作的信息，并允许迭代每个结果，如下所示：
        //遍历所有操作结果
        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            //获取操作的响应，可以是IndexResponse，UpdateResponse或DeleteResponse，
            // 它们都可以被视为DocWriteResponse实例
            DocWriteResponse itemResponse = bulkItemResponse.getResponse();

            if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                    || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                //处理index操作
                IndexResponse indexResponse = (IndexResponse) itemResponse;

            } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
                //处理update操作
                UpdateResponse updateResponse = (UpdateResponse) itemResponse;

            } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
                //处理delete操作
                DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
            }
        }

        //批量响应提供了用于快速检查一个或多个操作是否失败的方法：
        if (bulkResponse.hasFailures()) {
            //该方法只要有一个操作失败都会返回true
        }

        //如果想要查看操作失败的原因，则想要遍历所有操作结果
        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            if (bulkItemResponse.isFailed()) {//判断当前操作是否失败
                //获取失败原因，拿到了failure对象，想怎么玩都行
                BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
            }
        }


    }
}
