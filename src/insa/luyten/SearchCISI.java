package insa.luyten;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * This class is based on the Lucene Demo SearchFiles class.
 */
public class SearchCISI implements Runnable {

    public SearchCISI() {}

    public void run() {
        try {
            this.search();
        }        
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void search() throws Exception {
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_47);
        EnglishAnalyzer englishAnalyzer = new EnglishAnalyzer(Version.LUCENE_47);

        String[] fields = {"content", "title", "authors", "references"};
        Analyzer[] analysers = {englishAnalyzer, englishAnalyzer, standardAnalyzer, englishAnalyzer};

        String index = "index";
        String field = fields[0];
        String queries = null;
        int repeat = 0;
        boolean raw = false;
        String queryString = null;
        int hitsPerPage = 10;

        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        // :Post-Release-Update-Version.LUCENE_XY:

        Analyzer analyzer = analysers[0];

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

        // :Post-Release-Update-Version.LUCENE_XY:
        while (true) {
            System.out.println("\nChoose query field :");

            for (int i = 0; i < fields.length; i++) {
                System.out.println(i+1 + ". " + fields[i]);
            }

            int n = Integer.parseInt(in.readLine()) - 1;
            field = fields[n];
            analyzer = analysers[n];

            QueryParser parser = new QueryParser(Version.LUCENE_47, field, analyzer);

            if (queries == null && queryString == null) {                        // prompt the user
                System.out.println("Enter query: ");
            }

            String line = queryString != null ? queryString : in.readLine();

            if (line == null || line.length() == -1) {
                break;
            }

            line = line.trim();
            if (line.length() == 0) {
                break;
            }

            Query query = parser.parse(line);
            System.out.println("Searching for: " + query.toString(field));

            if (repeat > 0) {                           // repeat & time as benchmark
                Date start = new Date();
                for (int i = 0; i < repeat; i++) {
                    searcher.search(query, null, 100);
                }
                Date end = new Date();
                System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
            }

            //catch NewQueryException
            queryString = null; //by default
            int nq = 0;
            try{
            	this.doPagingSearch(in, searcher, query, field, hitsPerPage, raw, queries == null && queryString == null);
            }
            catch (NewQueryException e){
            	String newQueryString = e.getMessage();
            	System.out.println( "Is \"" + newQueryString + "\" a new query ?");

            	do{
            		System.out.println(" Enter (y)es or (n)o.");
            		String l =in.readLine();
            		if(l.length() == 1 && l.charAt(0) == 'y')
            			nq = 1;
            		else if (l.length() == 1 && l.charAt(0) == 'n')
            			nq = -1;
            	}while(nq == 0);

            	if(nq ==1){
            		queryString = newQueryString;
            	}
            }
            if(queryString != null && nq == 1) // it's okay
            	;
            else if(queryString != null)
            		break;

        }
        reader.close();
    }

    /**
     * This demonstrates a typical paging search scenario, where the search engine presents
     * pages of size n to the user. The user can then go to the next page if interested in
     * the next hits.
     *
     * When the query is executed for the first time, then only enough results are collected
     * to fill 5 result pages. If the user wants to page beyond this limit, then the query
     * is executed another time and all hits are collected.
     *
     */
    private void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, String field,
                                      int hitsPerPage, boolean raw, boolean interactive) throws IOException, NewQueryException{

        // Collect enough docs to show 5 pages
        TopDocs results = searcher.search(query, 5 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);

        while (true) {
            if (end > hits.length) {
                System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
                System.out.println("Collect more (y/n) ?");
                String line = in.readLine();
                if (line.length() == 0 || line.charAt(0) == 'n') {
                    break;
                }

                hits = searcher.search(query, numTotalHits).scoreDocs;
            }

            end = Math.min(hits.length, start + hitsPerPage);

            for (int i = start; i < end; i++) {
                if (raw) {                              // output raw format
                    System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
                    continue;
                }

                Document doc = searcher.doc(hits[i].doc);
                String index = doc.get("index");
                if (index != null) {
                    System.out.println((i+1) + ". " + index);
                    String title = doc.get("title");
                    if (title != null && (field.equals("content")) || field.equals("title")) {
                        System.out.println("   Title: " + doc.get("title"));
                    }
                    if (field.equals("authors")) {
                        System.out.println("   Authors: " + doc.get(field));
                    }
                    if (field.equals("references")) {
                        System.out.println("   References: " + doc.get(field));
                    }
                } else {
                    System.out.println((i+1) + ". " + "No path for this document");
                }

            }

            if (!interactive || end == 0) {
                break;
            }

            if (numTotalHits >= end) {
                boolean quit = false;
                while (true) {
                    System.out.print("Press ");
                    if (start - hitsPerPage >= 0) {
                        System.out.print("(p)revious page, ");
                    }
                    if (start + hitsPerPage < numTotalHits) {
                        System.out.print("(n)ext page, ");
                    }
                    System.out.println("(q)uit or enter number to jump to a page.");

                    String line = in.readLine();
                    //quit if no enter or q
                    if (line.length() == 0 || line.charAt(0)=='q') {
                        quit = true;
                        break;
                    }//just p
                    if (line.length() == 1 && line.charAt(0) == 'p') {
                        start = Math.max(0, start - hitsPerPage);
                        break;
                    }
                    //just n
                    else if (line.length() == 1 && line.charAt(0) == 'n') {
                        if (start + hitsPerPage < numTotalHits) {
                            start+=hitsPerPage;
                        }
                        break;
                    } else {
                    	//test if it's a word or a number 
                    	int page=0;
                    	try{
                    		page= Integer.parseInt(line);
                    	} //FD: if not a number, maybe a new query ?
                    	catch(NumberFormatException e){
                    		throw new NewQueryException(line);
                    	}
                    	if ((page - 1) * hitsPerPage < numTotalHits) {
                            start = (page - 1) * hitsPerPage;
                            break;
                        } else {
                            System.out.println("No such page");
                        }
                    }
                }
                if (quit) break;
                end = Math.min(numTotalHits, start + hitsPerPage);
            }
        }
    }
}
