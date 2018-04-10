import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.*;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
 *  Delete Index API
 */
public class Example02 {

    public static void main(String args[]) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")));

        DeleteIndexRequest request = new DeleteIndexRequest("twitter_two");//指定要删除的索引名称
        //可选参数：
        request.timeout(TimeValue.timeValueMinutes(2)); //设置超时，等待所有节点确认索引删除（使用TimeValue形式）
        // request.timeout("2m"); //设置超时，等待所有节点确认索引删除（使用字符串形式）

        request.masterNodeTimeout(TimeValue.timeValueMinutes(1));////连接master节点的超时时间(使用TimeValue方式)
        // request.masterNodeTimeout("1m");//连接master节点的超时时间(使用字符串方式)

        //设置IndicesOptions控制如何解决不可用的索引以及如何扩展通配符表达式
        request.indicesOptions(IndicesOptions.lenientExpandOpen());

        //同步执行
        DeleteIndexResponse deleteIndexResponse = client.indices().delete(request);

  /*    //异步执行删除索引请求需要将DeleteIndexRequest实例和ActionListener实例传递给异步方法：
        //DeleteIndexResponse的典型监听器如下所示：
        //异步方法不会阻塞并立即返回。
        ActionListener<DeleteIndexResponse> listener = new ActionListener<DeleteIndexResponse>() {
            @Override
            public void onResponse(DeleteIndexResponse deleteIndexResponse) {
                //如果执行成功，则调用onResponse方法;
            }

            @Override
            public void onFailure(Exception e) {
                //如果失败，则调用onFailure方法。
            }
        };
        client.indices().deleteAsync(request, listener);*/

        //Delete Index Response
        //返回的DeleteIndexResponse允许检索有关执行的操作的信息，如下所示：
        boolean acknowledged = deleteIndexResponse.isAcknowledged();//是否所有节点都已确认请求


        //如果找不到索引，则会抛出ElasticsearchException：
        try {
            request = new DeleteIndexRequest("does_not_exist");
            client.indices().delete(request);
        } catch (ElasticsearchException exception) {
            if (exception.status() == RestStatus.NOT_FOUND) {
                //如果没有找到要删除的索引，要执行某些操作
            }
        }
    }
}
