import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * 执行请求
 */
public class Example02 {
    static RestClient restClient =null;

    public static void main(String args[]) throws IOException {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")).build();
        synchronousRequest();
        asynchronousRequest();
    }

    /**
     * 发送同步请求
     */
    public static void synchronousRequest() throws IOException {
        //方式1：只提供谓词和终节点，这两个参数是必需要的参数
        Response response = restClient.performRequest("GET", "/");

        //方式2：提供谓词和终节点以及一些查询字符串参数来发送请求
        Map<String, String> params = Collections.singletonMap("pretty", "true");
        response = restClient.performRequest("GET", "/", params);

        //方式3：提供谓词和终节点以及可选查询字符串参数和org.apache.http.HttpEntity对象中包含的请求主体来发送请求
        params = Collections.emptyMap();
        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        //为HttpEntity指定ContentType非常重要，因为它将用于设置Content-Type请求头，以便Elasticsearch可以正确解析内容。
        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        response = restClient.performRequest("PUT", "/posts/doc/1", params, entity);

        //方式4：提供谓词，终节点，可选查询字符串参数，可选请求主体
        // 以及用于为每个请求尝试创建org.apache.http.nio.protocol.HttpAsyncResponseConsumer回调实例的可选工厂发送请求。
        // 控制响应正文如何从客户端的非阻塞HTTP连接进行流式传输。
        // 如果未提供，则使用默认实现，将整个响应主体缓存在堆内存中，最大为100 MB。
        params = Collections.emptyMap();
        HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory consumerFactory =
                new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(30 * 1024 * 1024);
        response = restClient.performRequest("GET", "/posts/_search", params, null, consumerFactory);

    }
    /**
     * 发送异步请求
     */
    public static  void asynchronousRequest(){
        //方式1： 提供谓词，终节点和响应监听器来发送异步请求，一旦请求完成，就会通知响应监听器，这三个参数是必需要的参数
        ResponseListener responseListener = new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                // 定义请求成功执行时需要做的事情
            }
            @Override
            public void onFailure(Exception exception) {
                // 定义请求失败时需要做的事情，即每当发生连接错误或返回错误状态码时做的操作。
            }
        };
        restClient.performRequestAsync("GET", "/", responseListener);

        //方式2： 提供谓词，终节点，一些查询字符串参数和响应监听器来发送异步请求
        Map<String, String>  params = Collections.singletonMap("pretty", "true");
        restClient.performRequestAsync("GET", "/", params, responseListener);

        //方式3：提供谓词，终节点，可选查询字符串参数，
        // org.apache.http.HttpEntity对象中包含的请求主体以及在请求完成后通知响应侦听器 来发送异步请求
        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        NStringEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        restClient.performRequestAsync("PUT", "/posts/doc/1", params, entity, responseListener);

        //方式4：提供谓词，终节点，可选查询字符串参数，可选请求主体
        // 以及用于为每个请求尝试创建org.apache.http.nio.protocol.HttpAsyncResponseConsumer回调实例的可选工厂发送异步请求。
        // 控制响应正文如何从客户端的非阻塞HTTP连接进行流式传输。
        // 如果未提供，则使用默认实现，将整个响应主体缓存在堆内存中，最大为100 MB。
        HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory  consumerFactory =
                new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(30 * 1024 * 1024);
        restClient.performRequestAsync("GET", "/posts/_search", params, null, consumerFactory, responseListener);
    }
}
