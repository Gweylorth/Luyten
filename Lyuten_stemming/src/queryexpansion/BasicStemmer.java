package queryexpansion;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

public class BasicStemmer extends AbstractDictionaryStemmer {

	public BasicStemmer(File dictionary) throws IOException{
		this.initIndexSearcher();
		this.createIndex(dictionary);
	}
	
	/**
	 * Algorithme tres simple qui tente de trouver le radical d'un mot
	 * Part de la chaine vide et ajoute les caracteres jusqu'a avoir un mot present dans le corpus de taille >= 3
	 * @param word
	 * @param indexSearcher
	 * @param qp
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public String stem(String word) throws IOException {
		TermQuery termQuery;
		char[] array = word.toCharArray();
		String stem = "";
		boolean stop = false;
		
		int i = 0;
		while(i < array.length && !stop){
			stem += array[i];
			termQuery = new TermQuery(new Term("entries", stem));
			TopDocs hits = indexSearcher.search(termQuery, 1);
			if (hits.totalHits > 0 && stem.length() >= 3)
				stop = true;
			i++;
		}
		
		return stem;
	}
}
