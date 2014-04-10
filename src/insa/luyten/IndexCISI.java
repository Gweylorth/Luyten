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
public class IndexCISI implements Runnable {

    private String docPath;
    private boolean skipNextLine = false;
    private String currentLine = "";

    public IndexCISI(String docPath) {
        this.docPath = docPath;
    }

    public void run() {

        String indexPath = "index";

        final File doc = this.getInputFile();

        Date start = new Date();
        try {
            System.out.println("Indexing to directory : " + indexPath + " ...");

            Directory dir = FSDirectory.open(new File(indexPath));
            Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_47);
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);

            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, config);
            indexDoc(writer, doc);
            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total ms");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private File getInputFile(){

        if (!this.docPath.endsWith("CISI.QRY")) {
            System.out.println("Invalid file entry, needs CISI.QRY");
            System.exit(1);
        }

        File doc = new File(docPath);
        if (!doc.exists() || !doc.canRead()){
            System.out.println("CISI file" + doc.getAbsolutePath() + "does not exist or is not readable.");
            System.exit(1);
        }
        return doc;
    }

    private void indexDoc(IndexWriter writer, File file) throws IOException {
        try {
            ArrayList<Document> docs = fileSplitter(file, ".I", ".W", ".T", ".A", ".B");
            int i = 0;
            System.out.println("");
            for (Document doc : docs) {
                writer.addDocument(doc);
                i++;
            }
            System.out.println("Added " + i + " documents.");
        } catch (IOException e) {
            writer.close();
            e.printStackTrace();
        }
    }

    /**
     * Splits huge file in many Documents
     * @param file File to split
     * @param separator String tag to split documents from one another
     * @param tags Used tags in documents description
     * @return List of Documents
     * @throws IOException
     */
    private ArrayList<Document> fileSplitter(File file, String separator, String... tags) throws IOException {
        ArrayList<Document> docs = new ArrayList<Document>();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        Pattern indexPattern = Pattern.compile(separator + " (\\d+)");
        while (this.currentLine != null) {
            // Skip reading another line if the current one has not already been parsed
            if (!this.skipNextLine)
                this.currentLine = reader.readLine();
            else
                this.skipNextLine = false;
            if (this.currentLine == null)
                break;

            // Handle indexing and document splitting
            if (this.currentLine.startsWith(separator)){
                Document d = new Document();
                Matcher m = indexPattern.matcher(this.currentLine);
                m.matches();
                int index = Integer.parseInt(this.currentLine.substring(m.start(1), m.end(1)));
                System.out.println("");
                System.out.println("Index : " + index);
                d.add(new IntField("index", index, Field.Store.YES));
                docs.add(d);
                continue;
            }

            // Check whether line starts with a tag
            String tagFound = this.findTags(this.currentLine, tags);

            // Handle lines according to tag found
            if (tagFound != null){
                Document lastDoc = docs.get(docs.size() - 1);
                this.processLine(reader, lastDoc, tagFound);
            }
        }
        return docs;
    }

    /**
     * Handle the current tag and add info to the current Document
     * @param reader
     * @param doc
     * @param tag
     * @throws IOException
     */
    private void processLine(BufferedReader reader, Document doc, String tag) throws IOException {
        switch (tag) {
            // Title found
            case ".T" :
                String title = this.multiLineRead(reader, ".W", ".A");
                System.out.println("Title : " + title);
                doc.add(new StringField("title", title, Field.Store.YES));
                break;
            // Authors found
            case ".A" :
                String authors = this.multiLineRead(reader, ".W");
                System.out.println("Authors : " + authors);
                doc.add(new StringField("authors", authors, Field.Store.YES));
                break;
            // Reference found
            case ".B" :
                String ref = reader.readLine();
                ref = ref.substring(1, ref.length() - 1); // Remove parenthesis
                System.out.println("Reference : " + ref);
                doc.add(new StringField("references", ref, Field.Store.YES));
                break;
            // Text found
            case ".W" :
                String content = this.multiLineRead(reader, ".I");
                doc.add(new TextField("content", content, Field.Store.YES));
                break;
            default :
                break;
        }
    }

    /**
     * Keeps concatenating lines to the result string while no stopping tag is found
     * @param reader The current BufferedReader
     * @return String, result of concatenation
     */
    private String multiLineRead(BufferedReader reader, String... tags) throws IOException {
        String result = "";
        this.skipNextLine = true;

        while ((this.currentLine = reader.readLine()) != null) {
            if (this.findTags(this.currentLine, tags) != null)
                break;
            else
                result = result.concat(this.currentLine + " ");
        }
        return result.trim();
    }

    /**
     * Check if line starts with any of the given tags
     * @param line String to check
     * @param tags Tags to look for
     * @return String, the first tag found, or null if none found
     */
    private String findTags(String line, String... tags) {
        for (String tag : tags) {
            if (line.startsWith(tag)){
                return tag;
            }
        }
        return null;
    }
}
