package queryexpansion;

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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public abstract class AbstractDictionaryStemmer {

	protected Directory dictionaryDirectory;
	protected Analyzer analyzer;
	protected IndexSearcher indexSearcher;
	
	public abstract String stem(String word) throws IOException;
	
	public void createIndex(File dictionary) throws IOException {
		System.out.println("Starting indexing dictionnary...");
		dictionaryDirectory = new SimpleFSDirectory(new File("files/dictionary"));
		analyzer = new StandardAnalyzer(Version.LUCENE_47);
		
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		IndexWriter indexWriter = new IndexWriter(dictionaryDirectory, indexWriterConfig);
		
		Document doc = new Document();
		
		String path = dictionary.getCanonicalPath();
		doc.add(new StringField("path", path, Field.Store.NO));
			
		Reader reader = new FileReader(dictionary);
		doc.add(new TextField("entries", reader));
			
		indexWriter.addDocument(doc);
		
		indexWriter.close();
		System.out.println("Dictionnary successfully indexed.");	
	}
	
	public void initIndexSearcher() throws IOException{
		IndexReader indexReader = DirectoryReader.open(dictionaryDirectory);
		indexSearcher = new IndexSearcher(indexReader);
	}
	
	public IndexSearcher getIndexSearcher(){
		return this.indexSearcher;
	}
}
