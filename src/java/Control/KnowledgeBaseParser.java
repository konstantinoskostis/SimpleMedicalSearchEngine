package Control;

import Model.Term;


import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class KnowledgeBaseParser extends DefaultHandler{
	
	
	private List<Term> terms; //list of all terms
	
	public KnowledgeBaseParser(){
		terms = new ArrayList<Term>();
	}
	
	public List<Term> getTerms(){
		return terms;
	}
	
	//temporary variables for parsing
	
	Term tmpTerm;
	String tmpValue;
	
	public void startDocument() throws SAXException {
        //todo: document start event
   }

   public void endDocument() throws SAXException {
       //todo: document end event
   }
	
   public void characters(char ch[], int start, int length)throws SAXException{
	   tmpValue = new String(ch,start,length);
   }
   
 //fires at <element>
   public void startElement(String uri, String localName,
           String qName, Attributes atts)
   throws SAXException {
       
       if(qName.equalsIgnoreCase("term")){
           tmpTerm = new Term();
       }                
   }
   
   public void endElement(String uri, String localName, String qName)
           throws SAXException {
       if (qName.equalsIgnoreCase("id")) {
    	   tmpTerm.setId(Integer.parseInt(tmpValue));
       } else if (qName.equalsIgnoreCase("name")) {
           tmpTerm.setName(tmpValue);
       } else if (qName.equalsIgnoreCase("def")) {
           tmpTerm.setDef(tmpValue);
       }else if (qName.equalsIgnoreCase("is_a")) {
           tmpTerm.setIs_a(tmpValue);
       }else if (qName.equalsIgnoreCase("synonym")) {
           //tmpTerm.setSynonym(tmpValue);
    	   tmpTerm.addSynonym(tmpValue);
       }else if (qName.equalsIgnoreCase("term")) {
           terms.add(tmpTerm);
       }
   }
   
   public List<Term> parseKnowledgeBase() throws Exception{
	   SAXParserFactory factory = SAXParserFactory.newInstance();
       SAXParser saxParser = factory.newSAXParser();
       KnowledgeBaseParser parser = new KnowledgeBaseParser();
       saxParser.parse("../IR_Project/Knowledge_Base.xml", parser);
       List<Term> terms = parser.getTerms();
       return terms;
   }
   
   /*
   public static void main(String args[]) throws Exception{
	   SAXParserFactory factory = SAXParserFactory.newInstance();
       SAXParser saxParser = factory.newSAXParser();
       KnowledgeBaseParser parser = new KnowledgeBaseParser();
       //saxParser.parse(new File("Knowledge_Base.xml"), parser);
       saxParser.parse("../IR_Project/Knowledge_Base.xml", parser);
       List<Term> terms = parser.getTerms();
       
       for(int i=0; i<terms.size(); i++){
    	   terms.get(i).printTerm();
       }
       
       System.out.println("Elements: "+terms.size());
   }*/
}
