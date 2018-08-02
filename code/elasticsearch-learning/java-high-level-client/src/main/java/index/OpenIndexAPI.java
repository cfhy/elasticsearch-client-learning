package index;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;

import java.io.IOException;

/**
 * Open Index API
 */
public class OpenIndexAPI {
    public static void example() throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")));

        OpenIndexRequest request = new OpenIndexRequest("twitter");//打开索引
        //可选参数：
        request.timeout(TimeValue.timeValueMinutes(2)); //设置超时，等待所有节点确认索引已打开（使用TimeValue形式）
        // request.timeout("2m"); //设置超时，等待所有节点确认索引已打开（使用字符串形式）

        request.masterNodeTimeout(TimeValue.timeValueMinutes(1));////连接master节点的超时时间(使用TimeValue方式)
        // request.masterNodeTimeout("1m");//连接master节点的超时时间(使用字符串方式)

        request.waitForActiveShards(2);//在打开索引API返回响应之前等待的活动分片副本的数量，以int形式表示。
        //request.waitForActiveShards(ActiveShardCount.ONE);//在打开索引API返回响应之前等待的活动分片副本的数量，以ActiveShardCount形式表示。

        //设置IndicesOptions控制如何解决不可用的索引以及如何扩展通配符表达式
        request.indicesOptions(IndicesOptions.strictExpandOpen());

        //同步执行
        OpenIndexResponse openIndexResponse = client.indices().open(request);

        /*//异步执行打开索引请求需要将OpenIndexRequest实例和ActionListener实例传递给异步方法：
        //OpenIndexResponse的典型监听器如下所示：
        //异步方法不会阻塞并立即返回。
        ActionListener<OpenIndexResponse> listener = new ActionListener<OpenIndexResponse>() {
            @Override
            public void onResponse(OpenIndexResponse openIndexResponse) {
                //如果执行成功，则调用onResponse方法;
            }

            @Override
            public void onFailure(Exception e) {
                //如果失败，则调用onFailure方法。
            }
        };
        client.indices().openAsync(request, listener);*/

        //Open Index Response
        //返回的OpenIndexResponse允许检索有关执行的操作的信息，如下所示：
        boolean acknowledged = openIndexResponse.isAcknowledged();//指示是否所有节点都已确认请求
        boolean shardsAcknowledged = openIndexResponse.isShardsAcknowledged();//指示是否在超时之前为索引中的每个分片启动了必需的分片副本数

    }
}
