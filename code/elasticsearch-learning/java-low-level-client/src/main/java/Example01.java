import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;

/**
 * 初始化操作 以及 配置可选参数
 */
public class Example01 {
    public static void main(String []args) throws IOException {
        /*RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http"),
                new HttpHost("localhost", 9201, "http")).build();
        restClient.close(); //关闭客户端，释放资源 */

        //配置可选参数
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("localhost", 9200, "http"));
        Header[] defaultHeaders = new Header[]{new BasicHeader("header", "value")};
        //设置每个请求需要发送的默认headers，这样就不用在每个请求中指定它们。
        builder.setDefaultHeaders(defaultHeaders);
        // 设置应该授予的超时时间，以防对相同的请求进行多次尝试。默认值是30秒，与默认socket超时时间相同。
        // 如果自定义socket超时时间，则应相应地调整最大重试超时时间。
        builder.setMaxRetryTimeoutMillis(10000);
        builder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(HttpHost host) {
                //设置一个侦听程序，每次节点发生故障时都会收到通知，这样就可以采取相应的措施。
                //Used internally when sniffing on failure is enabled.(这句话没搞懂啥意思)
            }
        });
        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                //设置允许修改默认请求配置的回调
                // （例如，请求超时，身份验证或org.apache.http.client.config.RequestConfig.Builder允许设置的任何内容）
                return requestConfigBuilder.setSocketTimeout(10000);
            }
        });
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                //设置允许修改http客户端配置的回调
                // （例如，通过SSL的加密通信，或者org.apache.http.impl.nio.client.HttpAsyncClientBuilder允许设置的任何内容）
                return httpClientBuilder.setProxy(new HttpHost("proxy", 9000, "http"));
            }
        });
    }
}
