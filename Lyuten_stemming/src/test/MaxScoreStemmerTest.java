package test;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.lucene.queryparser.classic.ParseException;
import org.tartarus.snowball.ext.PorterStemmer;

import queryexpansion.MaxScoreStemmer;
import queryexpansion.SRStemmer;
import queryexpansion.SimpleSuffixStemmer;

public class MaxScoreStemmerTest 
{
	public static final String DATA_DIRECTORY = "data";
	
	public static void main(String[] args)
	{
		try {
			System.out.println("          MAX SCORE STEMMER TEST              ");
			System.out.println("----------------------------------------------");
			test();
		} catch (IOException | ParseException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void test() throws IOException, ParseException {
		File dictionary = new File("files/brit-a-z.txt");
		File suffixesDictionary = new File("files/suffixes");
		MaxScoreStemmer stemmer = new MaxScoreStemmer(dictionary, suffixesDictionary);
		PorterStemmer porterStemmer = new PorterStemmer();
		
		System.out.println("Enter a word : ");
		Scanner sc = new Scanner(System.in);
		String word = sc.nextLine().split(" ")[0];
		
		System.out.println("Stemming '" + word + "'");
		
		String stem = porterstem(word, porterStemmer);
		System.out.println("Porter Stemmer : " + stem);
		String stem2 = stemmer.stem(word);
		System.out.println("Max. Score stemmer : " + stem2);
	}
	
	/**
	 * PorterStemmer
	 * @param word
	 * @return
	 */
	private static String porterstem(String word, PorterStemmer stemmer){
	       stemmer.setCurrent(word);
	       stemmer.stem();
	       return stemmer.getCurrent();
	}
}