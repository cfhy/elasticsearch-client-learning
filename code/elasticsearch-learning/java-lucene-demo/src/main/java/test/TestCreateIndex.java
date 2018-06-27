package test;

import lucene.Indexer;

/**
 * @description:
 * @author: yyb
 * @create: 2018/6/15.
 */
public class TestCreateIndex {

    public static void main(String[] args) {
        //索引指定的文档路径
        String relativelyPath=System.getProperty("user.dir");
        String indexDir=relativelyPath+"/java-lucene-demo/target/classes/index" ;
        String dataDir=relativelyPath+"/java-lucene-demo/target/classes/data" ;
        ////被索引数据的路径
        Indexer indexer = null;
        int numIndexed = 0;
        //索引开始时间
        long start = System.currentTimeMillis();
        try {
            indexer = new Indexer(indexDir);
            numIndexed = indexer.index(dataDir);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                indexer.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //索引结束时间
        long end = System.currentTimeMillis();
        System.out.println("索引：" + numIndexed + " 个文件 花费了" + (end - start) + " 毫秒");
    }
}
