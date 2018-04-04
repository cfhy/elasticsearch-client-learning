import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;

/**
 * 获取响应内容
 */
public class Example04 {
    public static void main(String args[]) throws IOException {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http"),
                new HttpHost("localhost", 9201, "http")).build();
        // Response response = restClient.performRequest("HEAD", "/s");
        Response response = restClient.performRequest("GET", "/");
        RequestLine requestLine = response.getRequestLine();//关于已执行请求的信息
        HttpHost host = response.getHost();//返回响应的主机
        int statusCode = response.getStatusLine().getStatusCode();//响应状态行，可以从中获取状态码
        Header[] headers = response.getHeaders();// 获取响应头
        String header=response.getHeader("content-type");// 获取指定名称的响应头
        String responseBody = EntityUtils.toString(response.getEntity());//响应体包含在org.apache.http.HttpEntity对象中

    }
}
