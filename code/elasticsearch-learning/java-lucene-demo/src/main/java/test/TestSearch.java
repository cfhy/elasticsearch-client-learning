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
        String q = "开放源代码程序库";
        String fileName ="中文";
        try {
           Searcher.search(indexDir, q);
           Searcher.searchBooleanQuery(indexDir, q,fileName);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
