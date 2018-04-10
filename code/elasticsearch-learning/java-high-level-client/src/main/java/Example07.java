import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.Map;

/**
 * @description:Delete API
 * @author: yyb
 * @create: 2018/4/10.
 */
public class Example07 {
    public static void main(String[]args) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")));

        DeleteRequest request = new DeleteRequest (
                "posts",//索引
                "doc",//类型
                "1");//文档ID

        //===============================可选参数====================================
        request.routing("routing");//设置routing值
        request.parent("parent");//设置parent值

        //设置超时：等待主分片变得可用的时间
        request.timeout(TimeValue.timeValueMinutes(2));//TimeValue方式
        request.timeout("1s");//字符串方式

        //刷新策略
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);//WriteRequest.RefreshPolicy实例方式
        request.setRefreshPolicy("wait_for");//字符串方式

        request.version(2);//设置版本
        request.versionType(VersionType.EXTERNAL);//设置版本类型

        //同步执行
        DeleteResponse deleteResponse = client.delete(request);


        //异步执行
        //DeleteResponse  的典型监听器如下所示：
        //异步方法不会阻塞并立即返回。
        ActionListener<DeleteResponse > listener = new ActionListener<DeleteResponse >() {
            @Override
            public void onResponse(DeleteResponse  getResponse) {
                //执行成功时调用。 Response以参数方式提供
            }

            @Override
            public void onFailure(Exception e) {
                //在失败的情况下调用。 引发的异常以参数方式提供
            }
        };
        //异步执行获取索引请求需要将DeleteRequest  实例和ActionListener实例传递给异步方法：
        client.deleteAsync(request, listener);

        //Delete Response
        //返回的DeleteResponse允许检索有关执行操作的信息，如下所示：
        String index = deleteResponse.getIndex();
        String type = deleteResponse.getType();
        String id = deleteResponse.getId();
        long version = deleteResponse.getVersion();
        ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            //处理成功分片数量少于总分片数量的情况
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                String reason = failure.reason();//处理潜在的失败
            }
        }

        //还可以检查文档是否被找到：
        DeleteRequest request1 = new DeleteRequest("posts", "doc", "does_not_exist");
        DeleteResponse deleteResponse1 = client.delete(request);
        if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
           //如果找不到要删除的文档，执行某些操作
        }

        //如果存在版本冲突，则会抛出ElasticsearchException：
        try {
            DeleteRequest request2 = new DeleteRequest("posts", "doc", "1").version(2);
            DeleteResponse deleteResponse2 = client.delete(request);
        } catch (ElasticsearchException exception) {
            if (exception.status() == RestStatus.CONFLICT) {
                //引发的异常表示返回了版本冲突错误
            }
        }
    }
}
