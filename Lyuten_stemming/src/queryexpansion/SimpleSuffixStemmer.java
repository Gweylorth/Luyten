package queryexpansion;

public class SimpleSuffixStemmer implements ISimpleStemmer {

	private String[] suffixes = { "able", "ible", "age", "ful", "less", "ing", "ness", "e", "s", 
									"ation", "er", "acy", "ism", "ly", "dom", "ical", "ment" };
	
	public String stem(String word){
		return removeSuffixes(word);
	}
	
	private String removeSuffixes(String word){
		for(String suf : suffixes){
			if (word.endsWith(suf) && (word.length() - suf.length() >= 3))
				return removeSuffixes(word.substring(0, word.length() - suf.length()));
		}
		
		return word;
	}
}
