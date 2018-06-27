package lucene;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * 根据索引搜索
 */
public class Searcher {

    /**
     * 单个查询
     * @param indexDir
     * @param q
     * @throws Exception
     */
    public static void search(String indexDir, String q) throws Exception {
        // 得到读取索引文件的路径
        Directory dir = FSDirectory.open(new File(indexDir));
        // 通过dir得到的路径下的所有的文件
        IndexReader reader = DirectoryReader.open(dir);
        // 建立索引查询器
        IndexSearcher is = new IndexSearcher(reader);
        // 实例化分析器
        Analyzer analyzer = new IKAnalyzer();
        // 建立查询解析器
        /**
         * 第一个参数是要查询的字段； 第二个参数是分析器Analyzer
         */
        QueryParser parser = new QueryParser(Version.LUCENE_45, "contents", analyzer);
        // 根据传进来的p查找
        Query query = parser.parse(q);
        // 计算索引开始时间
        long start = System.currentTimeMillis();
        // 开始查询
        /**
         * 第一个参数是通过传过来的参数来查找得到的query； 第二个参数是要出查询的行数
         */
        TopDocs hits = is.search(query, 10);
        // 计算索引结束时间
        long end = System.currentTimeMillis();
        System.out.println("匹配 " + q + " ，总共花费" + (end - start) + "毫秒" + "查询到" + hits.totalHits + "个记录");
        // 遍历hits.scoreDocs，得到scoreDoc
        /**
         * ScoreDoc:得分文档,即得到文档 scoreDocs:代表的是topDocs这个文档数组
         *
         * @throws Exception
         */
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(doc.get("fullPath"));
        }

        // 关闭reader
        reader.close();
    }

    /**
     * 多条件查询
     *
     * @param indexDir
     * @param content
     * @param fileName
     * @throws Exception
     */
    public static void searchBooleanQuery(String indexDir, String content, String fileName) throws Exception {
        Directory directory = FSDirectory.open(new File(indexDir));
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new IKAnalyzer();
        // 多条件必备神器,实际使用中一般是多目标搜索（根据 内容、文件名等等），
        BooleanQuery bQuery = new BooleanQuery();
        QueryParser parser = new QueryParser(Version.LUCENE_45, "contents", analyzer);
        Query query = parser.parse(content);
        bQuery.add(query, BooleanClause.Occur.MUST);
        bQuery.add(new TermQuery(new Term("fileName", fileName)), BooleanClause.Occur.MUST);

        TopDocs topDocs = indexSearcher.search(bQuery, 10);

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("fullPath"));
        }
        indexReader.close();
    }
}