/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Control;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.analysis.Analyzer;
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

/**
 *
 * @author konstantinos
 */
public class Process extends HttpServlet {


    private static final long serialVersionUID = 1L;
    String ret;
    int totalResults;
    int index = -1;

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String action = request.getParameter("action");
        if (action.equalsIgnoreCase("next")) { //go to next page of results
            String q = request.getParameter("queryText");
            List<String> docs = (List<String>) request.getSession().getAttribute("docs");
            int howManyLists = (docs.size()/5)+1; //find the size of the results list
            
            if(index >= howManyLists-1){
               index = howManyLists-1;
            }else{
                ++index;
            }

            request.setAttribute("query", q);
            request.setAttribute("index", index);
            request.setAttribute("docs", docs);
            request.setAttribute("total", totalResults);
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
        if (action.equalsIgnoreCase("prev")) { //go to previous page of results
            String q = request.getParameter("queryText");

            if(index <= 0){
                index = 0;
            }else{
                --index;
            }
            
            List<String> docs = (List<String>) request.getSession().getAttribute("docs");

            request.setAttribute("query", q);
            request.setAttribute("index", index);
            request.setAttribute("docs", docs);
            request.setAttribute("total", totalResults);
            request.getRequestDispatcher("index.jsp").forward(request, response);

        }
        if (action.equalsIgnoreCase("query")) { //new query submitted

            String radio = request.getParameter("autoExpChoice");
            

            String query = request.getParameter("queryText");
            String kString = request.getParameter("kValue");
            int k;
            if (kString.equalsIgnoreCase("")) {
                k = 5;
            } else {
                k = Integer.parseInt(kString);
            }
            //call apropriate functions
            index = 0;
            if (radio.equalsIgnoreCase("1")) {
                //ret = "Automatic query expansion";
                automaticQueryExpansion(query, k,index , request, response);
            } else if (radio.equalsIgnoreCase("2")) {
                //ret = "Related Terms";
                userTermSelection(query, k, index,request, response);
            } else if (radio.equalsIgnoreCase("0")) {
                //ret = "Default";
                Default(query, k,index ,request, response);
            } else if (radio.equalsIgnoreCase(null)) {
                //ret = " Just Null!!";
            }

        }
    }

    //usefull functions
    private void automaticQueryExpansion(String query, int k,int index,HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            String oldQuery = query;
            String newQuery = oldQuery;
            List<String> synonyms = processSynonyms(queryIndex(oldQuery, 2, k)); //get synonyms for this query
            String syns = "";
            for (int j = 0; j < synonyms.size(); j++) {
                if (j < (synonyms.size() - 1)) {
                    syns += synonyms.get(j) + ";";
                } else {
                    syns += synonyms.get(j);
                }
            }
            syns = syns.replaceAll(";", " ");
            newQuery += " " + syns; //put the synonyms to the old query and form a new one
            System.out.println(newQuery);
            List<String> docs = queryIndex(newQuery, 1, 0); //query index 1 and get the documents

            request.setAttribute("totalHits", totalResults);
            request.setAttribute("docs", docs);
            request.setAttribute("query", query);
            request.setAttribute("index", index);
            request.getRequestDispatcher("index.jsp").forward(request, response);

        } catch (Exception e) {
        }

    }

    //query index_phasek , k is a parameter
    private List<String> queryIndex(String q, int index, int k) throws Exception {
        List<String> list = new ArrayList<String>();
        String fname;
        q = cut(q);
        if (index == 1) {
            fname = this.getServletContext().getRealPath("/WEB-INF/indices/index_phase1");
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(fname)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
            QueryParser qParser;
            Query query;
            TopDocs results;
            ScoreDoc[] hits;

            qParser = new QueryParser(Version.LUCENE_35, "content", analyzer);
            query = qParser.parse(q);
            results = searcher.search(query, 200);
            hits = results.scoreDocs;
            totalResults = results.totalHits;

            for (int j = 0; j < hits.length; j++) {
                Document hitDoc = searcher.doc(hits[j].doc);
                list.add(hitDoc.get("content"));
            }
            searcher.close();
            reader.close();
            //System.out.println("docs from index 1 , list size:" + list.size());
        }
        if (index == 2) {
            fname = this.getServletContext().getRealPath("/WEB-INF/indices/index_phase2");
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(fname)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
            QueryParser qParser;
            Query query;
            TopDocs results;
            ScoreDoc[] hits;

            qParser = new QueryParser(Version.LUCENE_35, "combined", analyzer);
            query = qParser.parse(q);
            results = searcher.search(query, k);
            hits = results.scoreDocs;
            //System.out.println("For synonyms , matched: " + results.totalHits);
            String synonyms = "";
            for (int j = 0; j < hits.length; j++) {
                Document hitDoc = searcher.doc(hits[j].doc);
                synonyms = hitDoc.get("synonyms");
                if (!synonyms.contains("NO_SYNONYMS")) {
                    list.add(synonyms);
                }
            }
            searcher.close();
            reader.close();
            //System.out.println("Synonyms from index 2 , list size:" + list.size() + " , k:" + k);
        }
        //System.out.println("List size: "+list.size());
        return list;
    }

    private List<String> processSynonyms(List<String> synonyms) {
        List<String> l = new ArrayList<String>();
        for (int i = 0; i < synonyms.size(); i++) {
            l.add(cut(synonyms.get(i)));
        }
        return l;
    }//cuts some characters that may cause an exception to Lucene,and puts all the synonyms of a query in one list

    private String cut(String old) {
        char[] array = old.toCharArray();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == ':' || array[i] == '{' || array[i] == '}' || array[i] == '(' || array[i] == ')' || array[i] == '!') {
                array[i] = ' ';
            }
        }
        String n = new String(array);
        return n;
    }//clean up a string , cut illegal chars

    
    private void Default(String query, int k,int index, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            List<String> docs = queryIndex(query, 1, 0); //query index 1 and get the documents
           // System.out.println("docs returned: " + docs.size());

            request.setAttribute("docs", docs);
            request.setAttribute("totalHits", totalResults);
            request.setAttribute("query", query);
            request.setAttribute("index", index);
            request.getRequestDispatcher("index.jsp").forward(request, response);

        } catch (Exception e) {
        }
    }

    private void userTermSelection(String query, int k, int index,HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            List<String> docs = queryIndex(query, 1, 0); //query index 1 and get the documents

            //now get synonyms
            List<String> synonyms = processSynonyms(queryIndex(query, 2, k)); //get synonyms for this query
            String syns = "";
            for (int j = 0; j < synonyms.size(); j++) {
                if (j < (synonyms.size() - 1)) {
                    syns += synonyms.get(j) + ";";
                } else {
                    syns += synonyms.get(j);
                }
            }

            request.setAttribute("totalHits", totalResults);
            request.setAttribute("docs", docs);
            request.setAttribute("synonyms", syns);
            request.setAttribute("query", query);
            request.setAttribute("index", index);
            request.getRequestDispatcher("index.jsp").forward(request, response);

        } catch (Exception e) {
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        processRequest(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
