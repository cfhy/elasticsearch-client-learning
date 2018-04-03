import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.sniff.SniffOnFailureListener;
import org.elasticsearch.client.sniff.Sniffer;

import java.io.IOException;

/**
 * @description:sniffer
 * @author: yyb
 * @create: 2018/4/3.
 */
public class Example06 {

    /**
     * 方式一：创建RestClient实例后直接关联Sniffer
     * @throws IOException
     */
    public static void func1() throws IOException {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http"))
                .build();
        Sniffer sniffer = Sniffer.builder(restClient).build();

        sniffer.close();
        restClient.close();
    }

    /**
     * 修改默认的间隔时间
     * @throws IOException
     */
    public static void func2() throws IOException {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http"))
                .build();
        Sniffer sniffer = Sniffer.builder(restClient)
                .setSniffIntervalMillis(60000).build();
    }

    /**
     * 修改默认的间隔时间
     * @throws IOException
     */
    public static void func3() throws IOException {
        SniffOnFailureListener sniffOnFailureListener = new SniffOnFailureListener();
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200))
                //为RestClient实例设置失败监听器
                .setFailureListener(sniffOnFailureListener)
                .build();
        //当嗅探到失败时，不仅节点会在每次故障后得到更新，
        // 而且比平时更快地（默认是在失败后的一分钟内）再安排一轮嗅探，
        // 假设一切回归正常，我们希望尽快检测到它。
        // 可以在sniffer创建时通过setSniffAfterFailureDelayMillis方法对间隔时间进行定制。
        // 注意，这最后一个配置参数在嗅探失败时没有效果，
        // 以防在嗅探失败时不启用，就像上面解释的那样。
        Sniffer sniffer = Sniffer.builder(restClient)
                .setSniffAfterFailureDelayMillis(30000)
                .build();
        //为失败监听器设置Sniffer实例
        sniffOnFailureListener.setSniffer(sniffer);
    }

}
