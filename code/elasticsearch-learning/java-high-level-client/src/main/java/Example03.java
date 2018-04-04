import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/**
 * 发送异步请求基本示例
 */
public class Example03 {
    public static void example(){
       /* final CountDownLatch latch = new CountDownLatch(documents.length);
        for (int i = 0; i < documents.length; i++) {
            restClient.performRequestAsync(
                    "PUT",
                    "/posts/doc/" + i,
                    Collections.<String, String>emptyMap(),
                    //此处假设文档已存在 HttpEntity数组里
                    documents[i],
                    new ResponseListener() {
                        @Override
                        public void onSuccess(Response response) {
                            //处理返回的响应内容
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            // 由于通信错误或带有指示错误的状态码的响应，用于处理返回的异常
                            latch.countDown();
                        }
                    }
            );
        }
        latch.await();*/
    }
}
