package test;

import lucene.Searcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;

/**
 * @description:
 * @author: yyb
 * @create: 2018/6/19.
 */
public class TestSearch {

    public static void main(String[] args) throws IOException {
        //索引指定的文档路径
        String relativelyPath=System.getProperty("user.dir");
        String indexDir=relativelyPath+"/java-lucene-demo/target/classes/index" ;
        //我们要搜索的内容
        String q = "源代码";
        try {
           Searcher.search(indexDir, q);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Analyzer analyzer = new IKAnalyzer();
        String words = "开放源代码程序库";
        TokenStream stream = null;

        try {
            stream = analyzer.tokenStream("contents", words);
            //stream.reset();
            CharTermAttribute  offsetAtt = stream.addAttribute(CharTermAttribute.class);
            while (stream.incrementToken()) {
                System.out.println(offsetAtt.toString());
            }
            stream.end();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                stream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
