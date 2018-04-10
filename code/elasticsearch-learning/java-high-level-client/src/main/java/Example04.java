import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;

import java.io.IOException;

/**
 * Close Index API
 */
public class Example04 {
    public static void main(String args[]) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")));
        CloseIndexRequest request = new CloseIndexRequest("index");//关闭索引

        //可选参数：
        request.timeout(TimeValue.timeValueMinutes(2)); //设置超时，等待所有节点确认索引已关闭（使用TimeValue形式）
        // request.timeout("2m"); //设置超时，等待所有节点确认索引已关闭（使用字符串形式）

        request.masterNodeTimeout(TimeValue.timeValueMinutes(1));////连接master节点的超时时间(使用TimeValue方式)
        // request.masterNodeTimeout("1m");//连接master节点的超时时间(使用字符串方式)

        //设置IndicesOptions控制如何解决不可用的索引以及如何扩展通配符表达式
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        //同步执行
        CloseIndexResponse closeIndexResponse = client.indices().close(request);

         /*//异步执行打开索引请求需要将CloseIndexRequest实例和ActionListener实例传递给异步方法：
        //CloseIndexResponse的典型监听器如下所示：
        //异步方法不会阻塞并立即返回。
        ActionListener<CloseIndexResponse> listener = new ActionListener<CloseIndexResponse>() {
            @Override
            public void onResponse(CloseIndexResponse closeIndexResponse) {
                 //如果执行成功，则调用onResponse方法;
            }

            @Override
            public void onFailure(Exception e) {
                 //如果失败，则调用onFailure方法。
            }
        };
        client.indices().closeAsync(request, listener); */

        //Close Index Response
        //返回的CloseIndexResponse 允许检索有关执行的操作的信息，如下所示：
        boolean acknowledged = closeIndexResponse.isAcknowledged(); //指示是否所有节点都已确认请求

    }
}
