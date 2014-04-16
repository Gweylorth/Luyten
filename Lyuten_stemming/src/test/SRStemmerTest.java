package test;
import java.io.IOException;
import java.util.Scanner;

import org.apache.lucene.queryparser.classic.ParseException;
import org.tartarus.snowball.ext.PorterStemmer;

import queryexpansion.SRStemmer;
import queryexpansion.SimpleSuffixStemmer;

public class SRStemmerTest 
{
	public static final String DATA_DIRECTORY = "data";
	
	public static void main(String[] args)
	{
		try {
			System.out.println("          SUBSTITUTION RULES STEMMER TEST              ");
			System.out.println("-------------------------------------------------------");
			test();
		} catch (IOException | ParseException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void test() throws IOException, ParseException {
		SRStemmer stemmer = new SRStemmer();
		PorterStemmer porterStemmer = new PorterStemmer();
		
		System.out.println("Enter a word : ");
		Scanner sc = new Scanner(System.in);
		String word = sc.nextLine().split(" ")[0];
		
		System.out.println("Stemming '" + word + "'");
		
		String stem = porterstem(word, porterStemmer);
		System.out.println("Porter Stemmer : " + stem);
		String stem2 = stemmer.stem(word);
		System.out.println("SR-stemmer : " + stem2);
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