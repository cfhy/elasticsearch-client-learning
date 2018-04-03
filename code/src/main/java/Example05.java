import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * @description:常见的配置
 * @author: yyb
 * @create: 2018/4/3.
 */
public class Example05 {


    /**
     * 连接超时（默认为1秒）
     * 套接字超时（默认为30秒）
     * 最大重试超时时间（默认为30秒）
     */
    public static void timeout() {
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200))
                .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                    //该方法接收一个RequestConfig.Builder对象，对该对象进行修改后然后返回。
                    @Override
                    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                        return requestConfigBuilder.setConnectTimeout(5000) //连接超时（默认为1秒）
                                .setSocketTimeout(60000);//套接字超时（默认为30秒）
                    }
                })
                .setMaxRetryTimeoutMillis(60000);//调整最大重试超时时间（默认为30秒）
    }

    /**
     * 线程数
     */
    public static void threads() {
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultIOReactorConfig(
                                IOReactorConfig.custom().setIoThreadCount(1).build());
                    }
                });
    }

    /**
     * 基本认证
     */
    public static void authentication() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("user", "password"));

        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    // 该方法接收HttpAsyncClientBuilder的实例作为参数，对其修改后进行返回
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        httpClientBuilder.disableAuthCaching(); //禁用抢占式身份验证
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);//提供一个默认凭据
                    }
                });
    }


    /**
     * 加密通信
     */
    public static void encryptedCommunication(Path keyStorePath, String keyStorePass)
            throws KeyStoreException,
            NoSuchAlgorithmException,
            KeyManagementException,
            IOException,
            CertificateException {
        KeyStore truststore = KeyStore.getInstance("jks");
        try (InputStream is = Files.newInputStream(keyStorePath)) {
            truststore.load(is, keyStorePass.toCharArray());
        }
        SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
        final SSLContext sslContext = sslBuilder.build();
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "https"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setSSLContext(sslContext);
                    }
                });
    }
}


