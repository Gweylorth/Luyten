package insa.luyten;

/**
 * Created by Gweylorth on 08/04/2014.
 */
public class Main {

    public static void main(String[] args){
        IndexCISI index = new IndexCISI(".\\cisi\\CISI.QRY");
        index.run();
        SearchCISI search = new SearchCISI();
        search.run();
    }
}
