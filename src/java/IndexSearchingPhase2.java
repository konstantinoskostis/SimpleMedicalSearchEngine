import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class IndexSearchingPhase2 {

	
	private static String index1 = "../IR_Project/index_phase1";
	private static String index2 = "../IR_Project/index_phase2";
	
	public IndexSearchingPhase2(){
		
	}
	
	public Vector<String> retrieveOldQueries()throws Exception{
		Vector<String> queries = new Vector<String>();
		Scanner s = new Scanner(new File("../IR_Project/query.text"));
		String query = "";
		String line;
		while(s.hasNextLine()){
			line = s.nextLine();
			if(line.startsWith(".I")){
				if(!query.equals("")){
					queries.add(query);
					query = ""; //prepare buffer for next document
				}
			}else if(line.startsWith(".W")){
				//do nothing
			}else{
				query += line;
			}
		}
		//for last query
		queries.add(query);
		s.close();
		return queries;
	}
		
	public void submitNewQToIndex1(int k) throws Exception{
		Vector<String> oldQueries = retrieveOldQueries();
		Vector<String> newQueries = new Vector<String>();

		for(int i=0; i<oldQueries.size(); i++){
			List<String> querySynonyms = processSynonyms(getSynsFromKB(oldQueries.get(i), k));
			String newQuery = oldQueries.get(i);
			String syns = "";
			for(int j=0; j<querySynonyms.size(); j++){
				if(j < (querySynonyms.size()-1)){
					syns += querySynonyms.get(j)+",";
				}else{
					syns += querySynonyms.get(j);
				}
			}
			newQuery += " "+syns;
			newQueries.add(newQuery);
		}
		String resultsFile = "results_k_"+k+".txt";
		submitHelperIndex1(newQueries, resultsFile);

	}
	
	private void submitHelperIndex1(Vector<String> queries,String fileName) throws Exception{
		PrintStream ps = new PrintStream(new File("../IR_Project/"+fileName));
		IndexReader reader = IndexReader.open(FSDirectory.open(new File(index1)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		QueryParser parser = new QueryParser(Version.LUCENE_35, "content", analyzer);
		Query query;
		for(int i=0; i<queries.size(); i++){
			query = parser.parse(queries.get(i));
			TopDocs results = searcher.search(query, 200);
			ScoreDoc[] hits = results.scoreDocs;
			for (int j = 0; j < hits.length; j++) {
				Document hitDoc = searcher.doc(hits[j].doc);
				ps.print(i+1);
				ps.print("\tQ0");
				ps.print("\t"+hitDoc.get("docID")+"\t");
				ps.print(0+"\t"); 
				ps.print(hits[j].score);
				ps.print("\tIR_Project");
				ps.println();
			}//write to File
		}
		searcher.close();
		reader.close();
		ps.close();
	}
	
	
	public List<String> getSynsFromKB(String q,int k)throws Exception{
		List<String> list = new ArrayList<String>();
		IndexReader reader = IndexReader.open(FSDirectory.open(new File(index2)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		QueryParser qParser = new QueryParser(Version.LUCENE_CURRENT, "combined", analyzer);
		Query query;
		query = qParser.parse(q);
		TopDocs results = searcher.search(query, 100);
		ScoreDoc [] hits = results.scoreDocs;
		String synonyms = "";
		for(int j=0; j<hits.length; j++){
			Document hitDoc = searcher.doc(hits[j].doc);
			if(j<k){
				synonyms = hitDoc.get("synonyms");
				if(!synonyms.contains("NO_SYNONYMS")){
					list.add(synonyms.replaceAll(";", " "));
				}
				//synonyms += hitDoc.get("synonyms");
			}//j is in the first k docs
		}
		searcher.close();
		reader.close();
		
		return list;
	}
	
	public List<String> processSynonyms(List<String> synonyms){
		List<String> l = new ArrayList<String>();
		for(int i=0; i<synonyms.size(); i++){
			l.add(cut(synonyms.get(i)));
		}
		return l;
	}//cuts some characters that may cause an exception to Lucene,and puts all the synonyms of a query in one list
	
	private String cut(String old){
		char [] array = old.toCharArray();
		for(int i=0; i<array.length; i++){
			if(array[i] == ':' || array[i] == '{' || array[i] == '}' || array[i] == '(' ||  array[i] == ')' || array[i] == '!'
				|| array[i] == ';'){
				array[i] = ' ';
			}
		}
		String n = new String(array);
		return n;
	}
	
	
	public static void main(String args[]) throws Exception{
		IndexSearchingPhase2 is2 = new IndexSearchingPhase2();
		
		is2.submitNewQToIndex1(5);
		is2.submitNewQToIndex1(10);
		
		System.out.println("DONE!");
	}
	
}
