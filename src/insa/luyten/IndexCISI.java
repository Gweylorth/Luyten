package insa.luyten;

/**
 * Created by Gweylorth on 09/04/2014.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is based on the Lucene Demo IndexFiles class.
 * We want to try a more complex indexing than the original demo, on the contents of a single file (as opposed to a simple
 * indexing on multiple files).
 * For now, we aim it at CISI.QRY ( http://ir.dcs.gla.ac.uk/resources/test_collections/cisi/ )
 * We can make it more generic later to handle any kind of delimiter in a single file.
 * We'll try to make a new Analyzer that suits our needs next.
 * Let's see how far we can get!
 */
public class IndexCISI {

    private IndexCISI() {}

    public static void main(String[] args) {

        String usage = "IndexCISI"
                + " -doc DOC_PATH\n"
                + "This indexes the document in DOC_PATH, creating a Lucene index"
                + "that can be searched with SearchFiles";

        String indexPath = "index";
        String docPath = null;

        for (int i = 0; i<args.length;i++){
            if ("-doc".equals(args[i])){
                docPath = args[i+1];
            }
        }

        if (docPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        if (!docPath.endsWith("CISI.QRY")) {
            System.out.println("Invalid file entry, needs CISI.QRY");
            System.exit(1);
        }

        final File doc = new File(docPath);
        if (!doc.exists() || !doc.canRead()){
            System.out.println("CISI file" + doc.getAbsolutePath() + "does not exist or is not readable.");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory : " + indexPath + " ...");

            Directory dir = FSDirectory.open(new File(indexPath));
            Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_47);
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);

            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter writer = new IndexWriter(dir, config);
            indexDoc(writer, doc);
            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total ms");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    static void indexDoc(IndexWriter writer, File file) throws IOException {
        if (!file.canRead())
            return;

        try {
            ArrayList<Document> docs = fileSplitter(file, ".I", ".W", ".T", ".A", ".B");
        } catch (IOException e) {
            writer.close();
            e.printStackTrace();
        }
    }

    static ArrayList<Document> fileSplitter(File file, String separator, String... tags) throws IOException {
        ArrayList<Document> docs = new ArrayList<Document>();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        Pattern indexPattern = Pattern.compile(separator + " (\\d+)");
        String line = "";
        boolean skipLine = false;
        while (line != null) {
            // Skip reading another line if the current one has not already been parsed
            if (!skipLine)
               line = reader.readLine();
            else
                skipLine = false;
            if (line == null)
                break;

            // Handle indexing and document splitting
            if (line.startsWith(separator)){
                Document d = new Document();
                Matcher m = indexPattern.matcher(line);
                m.matches();
                int index = Integer.parseInt(line.substring(m.start(1), m.end(1)));
                System.out.println("");
                System.out.println("Index : " + index);
                d.add(new IntField("index", index, Field.Store.YES));
                docs.add(d);
                continue;
            }

            // Check whether line starts with a tag
            String tagFound = null;
            for (String tag : tags) {
                if (line.startsWith(tag)){
                    tagFound = tag;
                    break;
                }
            }

            // Handle lines according to tag found
            if (tagFound != null){
                // Switch on tags to handle them
                Document lastDoc = docs.get(docs.size() - 1);
                switch (tagFound) {
                    // Title found
                    case ".T" :
                        String title = "";
                        // Keep adding title lines until finding a content or author tag
                        while(!(line = reader.readLine()).startsWith(".W") && !line.startsWith(".A"))
                            title = title.concat(line + " ");
                        title.trim();
                        skipLine = true;
                        System.out.println("Title : " + title);
                        lastDoc.add(new StringField("title", title, Field.Store.YES));
                        break;
                    // Authors found
                    case ".A" :
                        String authors = "";
                        // Keep adding authors until finding a content tag
                        while(!(line = reader.readLine()).startsWith(".W"))
                            authors = authors.concat(line + " ");
                        authors.trim();
                        skipLine = true;
                        System.out.println("Authors : " + authors);
                        lastDoc.add(new StringField("authors", authors, Field.Store.YES));
                        break;
                    // Reference found
                    case ".B" :
                        String ref = reader.readLine();
                        ref = ref.substring(1, ref.length() - 1); // Remove parenthesis
                        System.out.println("Reference : " + ref);
                        lastDoc.add(new StringField("references", ref, Field.Store.YES));
                        break;
                    // Text found
                    case ".W" :
                        String content = "";
                        // Keep adding content until finding an index tag
                        while((line = reader.readLine()) != null && !line.startsWith(".I"))
                            authors = content.concat(line + " ");
                        content.trim();
                        skipLine = true;
                        lastDoc.add(new TextField("content", content, Field.Store.YES));
                        break;
                    default :
                        break;
                }
            }
        }
        return docs;
    }
}
