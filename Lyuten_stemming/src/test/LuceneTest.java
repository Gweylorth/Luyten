package test;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class LuceneTest 
{
	private static Directory indexDir;
	private static Analyzer analyzer;
	public static final String DATA_DIRECTORY = "data";
	
	public static void main(String[] args)
	{
		try {
			createIndex();
			searchIndex("hamburger");
			searchIndex("piano");
			searchIndex("the man is playing the piano while eating an hamburger");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (ParseException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void createIndex() throws IOException{
		System.out.println("Starting index creation...");
		indexDir = new RAMDirectory();
		analyzer = new StandardAnalyzer(Version.LUCENE_47);
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		IndexWriter indexWriter = new IndexWriter(indexDir, indexWriterConfig);
		
		File dataDir = new File(DATA_DIRECTORY);
		File[] subFiles = dataDir.listFiles();
		
		for(File file : subFiles){
			Document doc = new Document();
			
			String path = file.getCanonicalPath();
			doc.add(new StringField("path", path, Field.Store.NO));
			
			Reader reader = new FileReader(file);
			doc.add(new TextField("contents", reader));
			
			indexWriter.addDocument(doc);
		}
		indexWriter.close();
		System.out.println("Index successfully created.");
	}
	
	public static void searchIndex(String searchString) throws IOException, ParseException {
		System.out.println("Searching for '" + searchString + "'");
		IndexReader indexReader = DirectoryReader.open(indexDir);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		
		QueryParser queryParser = new QueryParser(Version.LUCENE_47, "contents", analyzer);
		Query query = queryParser.parse(searchString);
		
		TopDocs hits = indexSearcher.search(query, 3);
		System.out.println("Number of hits : " + hits.totalHits);
		
		for(ScoreDoc sdoc : hits.scoreDocs)
			System.out.println(sdoc.toString());
		
	}
}