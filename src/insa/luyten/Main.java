package insa.luyten;

import org.apache.lucene.demo.IndexFiles;
import org.apache.lucene.demo.SearchFiles;

/**
 * Created by Gweylorth on 08/04/2014.
 */
public class Main {

    public static void main(String[] args){
        IndexCISI.main(new String[]{"-doc", "S:\\GitHub repos\\Luyten\\cisi\\CISI.QRY"});
    }
}