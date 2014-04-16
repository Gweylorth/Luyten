package queryexpansion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

public class StemExpander {
	
	private HashSet<String> words;
	private List<String> suffixes;
	private IndexSearcher indexSearcher;
	
	public StemExpander(File suffixesDictionary, IndexSearcher searcher) throws IOException{
		this.indexSearcher = searcher;
		this.words = new HashSet<String>();
		this.suffixes = new ArrayList<String>();
		this.loadSuffixes(suffixesDictionary);
	}
	
	private void loadSuffixes(File suffixesDictionary) throws IOException {
		String currentLine;
		BufferedReader br = new BufferedReader(new FileReader(suffixesDictionary));
		
		while((currentLine = br.readLine()) != null){
			String[] tokens = currentLine.split(" ");
			suffixes.add(tokens[0]);
		}
		br.close();
	}
	
	public HashSet<String> expandFromStem(String stem) throws IOException{
		this.words.clear();
		if (isInDictionary(stem))
			words.add(stem);
		
		addSuffixes(stem);
		
		return words;
	}
	
	private void addSuffixes(String str) throws IOException {
		for(String suf : suffixes){
			String candidate = str.concat(suf);
		
			if (isInDictionary(candidate)){
				words.add(candidate);
				addSuffixes(candidate);
			}
		}
	}
	
	public boolean isInDictionary(String str) throws IOException{
		TermQuery query = new TermQuery(new Term("entries", str));
		TopDocs hits = indexSearcher.search(query, 1);
		
		return (hits.totalHits > 0);
	}
}
