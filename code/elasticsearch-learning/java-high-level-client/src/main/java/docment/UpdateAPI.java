package docment;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * @description:Update API
 * @author: yyb
 * @create: 2018/4/10.
 */
public class UpdateAPI {
    public static void main(String[]args) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")));

        UpdateRequest request = new UpdateRequest  (
                "test",//索引
                "_doc",//类型
                "1");//文档ID

        //更新API允许通过使用脚本或传递部分文档来更新现有文档。

        //使用脚本
        //方式1：该脚本可以作为内联脚本提供：
        Map<String, Object> parameters = singletonMap("count", 4);//脚本参数
        //使用painless语言和上面的参数创建一个内联脚本
        Script inline = new Script(ScriptType.INLINE, "painless", "ctx._source.field += params.count", parameters);
        request.script(inline);

        //方式2：引用名称为increment-field的脚本,改脚本定义的位置还没搞清楚。
        Script stored =
                new Script(ScriptType.STORED, null, "increment-field", parameters);
        request.script(stored);

        //只更新部分
        //更新部分文档时，更新的部分文档将与现有文档合并。

        //方式1：使用字符串形式
        UpdateRequest request1 = new UpdateRequest("posts", "doc", "1");
        String jsonString = "{" +
                "\"updated\":\"2017-01-01\"," +
                "\"reason\":\"daily update\"" +
                "}";
        request1.doc(jsonString, XContentType.JSON);

        //方式2：使用Map形式，会被自动转为json格式
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("updated", new Date());
        jsonMap.put("reason", "daily update");
        UpdateRequest request2 = new UpdateRequest("posts", "doc", "1")
                .doc(jsonMap);


        //方式3：使用XContentBuilder形式
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.field("updated", new Date());
            builder.field("reason", "daily update");
        }
        builder.endObject();
        UpdateRequest request3 = new UpdateRequest("posts", "doc", "1")
                .doc(builder);


        //方式4：使用Object key-pairs形式
        UpdateRequest request4 = new UpdateRequest("posts", "doc", "1")
                .doc("updated", new Date(),
                        "reason", "daily update");


        //如果文档尚不存在，则可以使用upsert方法定义一些将作为新文档插入的内容：
        //与部分文档更新类似，可以使用接受String，Map，XContentBuilder或Object key-pairs的方式来定义upsert文档的内容。
        String jsonString1 = "{\"created\":\"2017-01-01\"}";
        request.upsert(jsonString1, XContentType.JSON);

        //=========================可选参数===========================
        request.routing("routing");//设置routing值
        request.parent("parent");//设置parent值

        //设置超时：等待主分片变得可用的时间
        request.timeout(TimeValue.timeValueSeconds(1));//TimeValue方式
        request.timeout("1s");//字符串方式

        //刷新策略
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);//WriteRequest.RefreshPolicy实例方式
        request.setRefreshPolicy("wait_for");//字符串方式

        //如果要更新的文档在获取或者索引阶段已被另一操作更改，则重试更新操作的次数
        request.retryOnConflict(3);

        request.version(2);//设置版本

        request.fetchSource(true); //启用_source检索，默认为禁用

        //为特定字段配置_source_include
        String[] includes = new String[]{"updated", "r*"};
        String[] excludes = Strings.EMPTY_ARRAY;
        request.fetchSource(new FetchSourceContext(true, includes, excludes));

        //为指定字段配置_source_exclude
        String[] includes1 = Strings.EMPTY_ARRAY;
        String[] excludes1 = new String[]{"updated"};
        request.fetchSource(new FetchSourceContext(true, includes1, excludes1));

        request.detectNoop(false);//禁用noop检测

        //无论文档是否存在，脚本都必须运行，即如果脚本尚不存在，则脚本负责创建文档。
        request.scriptedUpsert(true);

        //如果不存在，则表明部分文档必须用作upsert文档。
        request.docAsUpsert(true);

        //设置在继续更新操作之前必须激活的分片副本的数量。
        request.waitForActiveShards(2);
        //使用ActiveShardCount方式，可以是ActiveShardCount.ALL，ActiveShardCount.ONE或ActiveShardCount.DEFAULT（默认值）
        request.waitForActiveShards(ActiveShardCount.ALL);

        //同步执行
        UpdateResponse updateResponse = client.update(request);


        //异步执行
        //DeleteResponse  的典型监听器如下所示：
        //异步方法不会阻塞并立即返回。
        ActionListener<UpdateResponse > listener = new ActionListener<UpdateResponse >() {
            @Override
            public void onResponse(UpdateResponse  updateResponse) {
                //执行成功时调用。 Response以参数方式提供
            }

            @Override
            public void onFailure(Exception e) {
                //在失败的情况下调用。 引发的异常以参数方式提供
            }
        };
        //异步执行获取索引请求需要将UpdateRequest  实例和ActionListener实例传递给异步方法：
        client.updateAsync(request, listener);

        //Update Response
        //返回的UpdateResponse允许检索有关执行操作的信息，如下所示：
        String index = updateResponse.getIndex();
        String type = updateResponse.getType();
        String id = updateResponse.getId();
        long version = updateResponse.getVersion();
        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            //处理第一次创建文档的情况（upsert）
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            //处理文档被更新的情况
        } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
            //处理文档已被删除的情况
        } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
            //处理文档未受更新影响的情况，即文档上未执行任何操作（noop）
        }

        //当通过fetchSource方法在UpdateRequest中启用源检索时，响应会包含已更新文档：
        GetResult result = updateResponse.getGetResult();//获取已更新的文档
        if (result.isExists()) {
            String sourceAsString = result.sourceAsString();//获取已更新的文档源（String方式）
            Map<String, Object> sourceAsMap = result.sourceAsMap();//获取已更新的文档源（Map方式）
            byte[] sourceAsBytes = result.source();//获取已更新的文档源（byte[]方式）
        } else {
            //处理不返回文档源的场景（默认就是这种情况）
        }


        //也可以检查分片失败：
        ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            //处理成功分片数量少于总分片数量的情况
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                String reason = failure.reason();//处理潜在的失败
            }
        }

        //当针对文档不存在时，响应404状态码，将引发ElasticsearchException，需要按如下方式处理：
        UpdateRequest request5 = new UpdateRequest("posts", "type", "does_not_exist").doc("field", "value");
        try {
            UpdateResponse updateResponse5 = client.update(request);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                //处理由于文档不存在抛出的异常
            }
        }

        //如果存在版本冲突，则会抛出ElasticsearchException：
        UpdateRequest request6 = new UpdateRequest("posts", "doc", "1")
                .doc("field", "value")
                .version(1);
        try {
            UpdateResponse updateResponse6 = client.update(request);
        } catch(ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                //引发的异常表示返回了版本冲突错误
            }
        }
    }
}
