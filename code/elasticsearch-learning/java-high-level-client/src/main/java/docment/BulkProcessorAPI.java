package docment;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class BulkProcessorAPI {
    private static final Logger logger = LoggerFactory.getLogger(BulkProcessorAPI.class);
    public static void main(String[] args) throws InterruptedException {
        //高级客户端提供了批量处理器以协助批量请求
        //BulkRequest可以在一次请求中执行多个索引，更新或者删除操作。一次请求至少得有一个操作。
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")));
        //创建BulkProcessor.Listener
        BulkProcessor.Listener listener1 = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                //在每次执行BulkRequest之前调用此方法
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                //在每次执行BulkRequest之后调用此方法
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                //执行BulkRequest失败时调用此方法
            }
        };
        //通过从BulkProcessor.Builder调用build（）方法来创建BulkProcessor。
        //RestHighLevelClient.bulkAsync（）方法将用来执行BulkRequest。
        BulkProcessor bulkProcessor = BulkProcessor.builder(client::bulkAsync, listener1).build();

        //配置
        BulkProcessor.Builder builder = BulkProcessor.builder(client::bulkAsync, listener1);
        //设置何时刷新新的批量请求,根据当前已添加的操作数量（默认为1000，使用-1禁用它）
        builder.setBulkActions(500);//操作数为500时就刷新请求
        //设置何时刷新新的批量请求,根据当前已添加的操作大小（默认为5Mb，使用-1禁用它）
        builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB));//操作大小为1M时就刷新请求
        //设置允许执行的并发请求数（默认为1，使用0只允许执行单个请求）
        builder.setConcurrentRequests(0);//不并发执行
        //设置刷新间隔时间，如果超过了间隔时间，则随便刷新一个BulkRequest挂起（默认为未设置）
        builder.setFlushInterval(TimeValue.timeValueSeconds(10L));
        //设置一个最初等待1秒,最多重试3次的常量退避策略。
        // 有关更多选项，请参阅BackoffPolicy.noBackoff（），BackoffPolicy.constantBackoff（）和BackoffPolicy.exponentialBackoff（）。
        builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 3));

        IndexRequest one = new IndexRequest("posts", "doc", "1").
                source(XContentType.JSON, "title",
                        "In which order are my Elasticsearch queries executed?");
        IndexRequest two = new IndexRequest("posts", "doc", "2")
                .source(XContentType.JSON, "title",
                        "Current status and upcoming changes in Elasticsearch");
        IndexRequest three = new IndexRequest("posts", "doc", "3")
                .source(XContentType.JSON, "title",
                        "The Future of Federated Search in Elasticsearch");

        bulkProcessor.add(one);
        bulkProcessor.add(two);
        bulkProcessor.add(three);

        //这些请求将由BulkProcessor执行，BulkProcessor负责为每个批量请求调用BulkProcessor.Listener。
        //侦听器提供访问BulkRequest和BulkResponse的方法：
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                //在每次执行BulkRequest之前调用，通过此方法可以获取将在BulkRequest中执行的操作数
                int numberOfActions = request.numberOfActions();
                logger.debug("Executing bulk [{}] with {} requests",
                        executionId, numberOfActions);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {
                //在每次执行BulkRequest后调用，通过此方法可以获取BulkResponse是否包含错误
                if (response.hasFailures()) {
                    logger.warn("Bulk [{}] executed with failures", executionId);
                } else {
                    logger.debug("Bulk [{}] completed in {} milliseconds",
                            executionId, response.getTook().getMillis());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                //如果BulkRequest失败，通过调用此方法可以获取失败
                logger.error("Failed to execute bulk", failure);
            }
        };
        //将所有请求添加到BulkProcessor后，需要使用两种可用的关闭方法之一关闭其实例。
        //awaitClose（）方法可用于等待所有请求都已处理或过了指定的等待时间：

        boolean terminated = bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);
        //如果所有批量请求都已完成，则该方法返回true;如果在所有批量请求完成之前等待时间已过，则返回false

        //close（）方法可用于立即关闭BulkProcessor：

        bulkProcessor.close();
        //两种方法在关闭处理器之前会刷新已添加到处理器的请求，并且还会禁止将任何新请求添加到处理器。
    }
}
