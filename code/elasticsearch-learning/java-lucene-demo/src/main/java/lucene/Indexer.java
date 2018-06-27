package lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.nio.file.Paths;

/**
 * @description:
 * @author: yyb
 * @create: 2018/6/15.
 */
public class Indexer {

    // 写索引实例
    private IndexWriter writer;

    /**
     * 构造方法 实例化IndexWriter
     *
     * @param indexDir
     * @throws IOException
     */
    public Indexer(String indexDir) throws IOException {
        //得到索引所在目录的路径
        Directory directory = FSDirectory.open(new File(indexDir));
        // 中文分词器
        Analyzer analyzer = new IKAnalyzer();
        //保存用于创建IndexWriter的所有配置。
        IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_45, analyzer);
        //实例化IndexWriter
        writer = new IndexWriter(directory, iwConfig);
    }

    /**
     * 关闭写索引
     *
     * @return 索引了多少个文件
     * @throws Exception
     */
    public void close() throws IOException {
        writer.close();
    }

    /**
     * 创建索引
     * @param dataDir 数据文件路径
     * @return
     * @throws Exception
     */
    public int index(String dataDir) throws Exception {
        File[] files = new File(dataDir).listFiles();
        for (File file : files) {//索引指定文件
            //输出索引文件的路径
            System.out.println("索引文件：" + file.getCanonicalPath());
            //获取文档，文档里再设置每个字段
            Document doc = new Document();
            //把设置好的索引加到Document里，以便在确定被索引文档
            doc.add(new TextField("contents", this.getText(file),Field.Store.YES));
            //Field.Store.YES：把文件名存索引文件里，为NO就说明不需要加到索引文件里去
            doc.add(new TextField("fileName", file.getName(), Field.Store.YES));
            //把完整路径存在索引文件里
            doc.add(new TextField("fullPath", file.getCanonicalPath(), Field.Store.YES));
            //开始写入,就是把文档写进了索引文件里去了；
            writer.addDocument(doc);
        }
        //返回索引了多少个文件
        return writer.numDocs();
    }

    private String getText (File file) throws IOException {
        String content="";
        Reader fr = new FileReader(file);
        char[] buf = new  char[1024];
        int num = 0;
        while((num=fr.read(buf))!=-1)
        {
            content+=new String(buf,0,num);
        }
        fr.close();
        return content;
    }
}