/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author konstantinos
 */
import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Phase1 {

	String indexLocation;

	public Phase1(){
		indexLocation = "../IR_Project/index_phase1";
		try{
			Directory dir = FSDirectory.open(new File(indexLocation)); //create new Directory
			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35); //create the analyzer
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35,analyzer); //create the writer's configuration
			config.setOpenMode(OpenMode.CREATE); //create new index , remove previous ones

			int docID = 1; //the id of a document
			String content = ""; //the content of a document

			IndexWriter writer = new IndexWriter(dir, config);

			Scanner s = new Scanner(new File("../IR_Project/med.all"));
			String line;
			while(s.hasNextLine()){
				line = s.nextLine();
				if(line.startsWith(".I")){
					if(!content.equals("")){
						docAddition(writer, String.valueOf(docID), content);
						++docID; //saw a new document
						content = ""; //prepare buffer for next document
					}
				}else if(line.startsWith(".W")){
					//do nothing
				}else{
					content += line;
				}
			}
			//for the last document
			docAddition(writer, String.valueOf(docID), content);
			//close streams
			s.close();
			writer.close();

		}catch(Exception e){

		}

	}

	private static void docAddition(IndexWriter w,String id,String content)throws Exception{
		Document d = new Document();
		Field idField = new Field("docID",id,Field.Store.YES, Field.Index.NO);
		d.add(idField);
		Field contentField = new Field("content",content,Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES);
		d.add(contentField);
		w.addDocument(d);
	}

	private Vector<String> retrieveQueries()throws Exception{
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

	//query the index and store the results
	public void searchAndStore() throws Exception{
		Vector<String> queries = retrieveQueries();
		PrintStream ps = new PrintStream(new File("../IR_Project/Phase1Results.txt"));
		IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexLocation)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		QueryParser parser = new QueryParser(Version.LUCENE_35, "content", analyzer);
		Query query;
		//MyDocumentCollection dc = new MyDocumentCollection();
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
				//dc.addDoc(hitDoc.get("docID"), hits[j].score);
			}//write to File
			/*dc.sort();
			dc.toTrecFormat(i+1, ps);
			dc.clearAll();*/
		}
		searcher.close();
		reader.close();
		ps.close();
	}

	public static void main(String args[])throws Exception{
		Phase1 p = new Phase1();
		p.searchAndStore();
	}

}
