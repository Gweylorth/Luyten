package insa.luyten;

import org.apache.lucene.demo.IndexFiles;
import org.apache.lucene.demo.SearchFiles;

/**
 * Created by Gweylorth on 08/04/2014.
 */
public class Main {

    public static void main(String[] args){
        IndexFiles.main(new String[]{"-docs", "C:\\Users\\Orhin\\Downloads\\lucene-4.7.1-src\\lucene-4.7.1\\demo\\src"});
        try {
            SearchFiles.main(new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
