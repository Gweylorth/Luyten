package queryexpansion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;

public class MaxScoreStemmer extends AbstractDictionaryStemmer {

	private List<String> suffixes;
	private File suffixesDictionary;
	
	public MaxScoreStemmer(File dictionary, File suffixesDictionary) throws IOException{
		this.suffixesDictionary = suffixesDictionary;
		this.suffixes = new ArrayList<String>();
		this.createIndex(dictionary);
		this.initIndexSearcher();
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
	
	public String stem(String word){
		try {
			return removeSuffixes(word, 0);
		} catch (IOException | ParseException e) {
			System.err.println(e.getMessage());
		}
		return null;
	}
	
	private String removeSuffixes(String word, int score) throws IOException, ParseException{
		for(String suf : suffixes){
			if (word.endsWith(suf)){
				String newWord = word.substring(0, word.length() - suf.length());
				if (newWord.length() >= 3){
					int newScore = nbSameStemWords(newWord);
					if (newScore >= score)
						return removeSuffixes(newWord, newScore);
				}
			}
		}
		
		return word;
	}
	
	public int nbSameStemWords(String word) throws IOException, ParseException {
		StemExpander expander = new StemExpander(suffixesDictionary, this.indexSearcher);
		return expander.expandFromStem(word).size();
	}
}
