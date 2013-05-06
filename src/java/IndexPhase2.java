import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelectorResult;
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

import Model.Term;
import Control.KnowledgeBaseParser;;

public class IndexPhase2 {

	String indexLocation;
	
	public IndexPhase2(){
		
	}
	
	public  void createIndexPhase2(){
		
		try{
			
			indexLocation = "../IR_Project/index_phase2";
			Directory dir = FSDirectory.open(new File(indexLocation)); //create new Directory
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT); //create the analyzer
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_CURRENT,analyzer); //create the writer's configuration
			config.setOpenMode(OpenMode.CREATE); //create new index , remove previous ones
			IndexWriter writer = new IndexWriter(dir, config);
			
			KnowledgeBaseParser parser = new KnowledgeBaseParser();
			List<Term> terms = parser.parseKnowledgeBase();
			for(Term t:terms){
				indexTerms(writer, t);
			}
			
			writer.close();
		}catch(Exception e){
			
		}
		
	}//create the index
	
	private void indexTerms(IndexWriter iw,Term t){
		
		try{
			
			Document doc = new Document();
			
			Field f2 = new Field("is_a",t.getIs_a(),Field.Store.YES,Field.Index.NO,Field.TermVector.NO);
			doc.add(f2);
			
			String synonyms = "";
			if(t.getList().size() > 0){
				int lSize = t.getList().size();
				for (int i = 0; i < lSize; i++) {
					if (i < (lSize - 1)) {
						synonyms += t.getList().get(i) + ";";
					} else {
						synonyms += t.getList().get(i);
					}
				}
			}else{
				synonyms = "NO_SYNONYMS";
			}
			
			Field f3 = new Field("synonyms",synonyms,Field.Store.YES,Field.Index.NO,Field.TermVector.NO);
			doc.add(f3);
			
			String combined = t.getName()+" "+t.getDef();
			Field f1 = new Field("combined",combined,Field.Store.YES,Field.Index.ANALYZED,Field.TermVector.YES);
			doc.add(f1);
			
			if (iw.getConfig().getOpenMode() == OpenMode.CREATE) {
	            // New index, so we just add the document (no old document can be there):
                              iw.addDocument(doc);
                         }
		
			/*
			Document doc = new Document();
			
			Field f2 = new Field("is_a",t.getIs_a(),Field.Store.YES,Field.Index.NO,Field.TermVector.NO);
			doc.add(f2);
			
			String synonyms = "";
			int lSize = t.getList().size();
			for(int i=0; i<lSize; i++){
				if(i < (lSize-1)){
					synonyms += t.getList().get(i)+",";
				}else{
					synonyms += t.getList().get(i);
				}
			}
			
			//System.out.println(synonyms);
			Field f3 = new Field("synonyms",synonyms,Field.Store.YES,Field.Index.NO,Field.TermVector.NO);
			doc.add(f3);
			
			String combined = t.getName()+" "+t.getDef();
			Field f1 = new Field("combined",combined,Field.Store.YES,Field.Index.ANALYZED,Field.TermVector.YES);
			doc.add(f1);
			
			if (iw.getConfig().getOpenMode() == OpenMode.CREATE) {
	            // New index, so we just add the document (no old document can be there):
	            iw.addDocument(doc);
	        }*/
			
		}catch(Exception e){
			
		}
		
	}
	
	public static void main(String args[]){
		new IndexPhase2().createIndexPhase2();
		System.out.println("Created!");
	}
	
}
