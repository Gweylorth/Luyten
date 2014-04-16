package queryexpansion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SRStemmer implements ISimpleStemmer {

	private Map<String, String> rules;
	
	public SRStemmer() throws IOException{
		rules = new HashMap<String, String>();
		loadRules();
	}
	
	private void loadRules() throws IOException{
		String currentLine;
		BufferedReader br = new BufferedReader(new FileReader("files/rules"));
		
		while((currentLine = br.readLine()) != null){
			parseLine(currentLine);
		}
		br.close();
		rules = sortByKeys(rules);
	}
	
	private void parseLine(String line){
		if (line.compareTo("") == 0 || line.startsWith("%"))
			return;
		line = line.replaceAll("\\s+","");
		String[] tokens = line.split(":=");
		if (tokens[1].compareTo("null") == 0)
			tokens[1] = "";
		rules.put(tokens[0],tokens[1]);
	}
	
	public String stem(String word){
		//On regarde, pour chaque regle, si on peut l'appliquer
		for(String key : rules.keySet()){
			if (word.endsWith(key))
				return stem(word.substring(0, word.length() - key.length()) + rules.get(key));
		}
		
		return word;
	}
	
	 public static Map<String,String> sortByKeys(Map<String,String> map){
	        List<String> keys = new LinkedList<String>(map.keySet());
	        Collections.sort(keys, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					if (o1.length() > o2.length())
						return -1;
					else if (o1.length() < o2.length())
						return 1;
					else
						return 0;
				}
	        	
	        });
	     
	        Map<String,String> sortedMap = new LinkedHashMap<String,String>();
	        for(String key: keys){
	            sortedMap.put(key, map.get(key));
	        }
	     
	        return sortedMap;
	 }	
}
