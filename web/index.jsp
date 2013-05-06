<%-- 
    Document   : index
    Created on : Jan 16, 2013, 11:58:37 PM
    Author     : konstantinos
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="java.util.*" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Medical Search Engine(Information Retrieval-CS@AUEB by Kostis Konstantinos)</title>

        <script>
            function addSynonym(synsForm)
            {
                var i = synsForm.synsList.selectedIndex;
                var t = synsForm.synsList.options[i].text;
                document.getElementById("queryText").value += ","+t;

            }

        </script>


        <script>
            function validate()
            {
                var queryText=document.forms["theForm"]["queryText"].value;
                var query = document.getElementById(queryText);
                if(queryText=="" || queryText=="Type your query here..."){
                    alert('You must enter a valid query!!!');
                    return false;
                }
            }
        </script>

        <script>
            function showDoc(t)
            {
                var text = new String(t.abbr);
                newwindow2=window.open('','name','height=200,width=350, scrollbars=yes,resizable=yes,top=150,left=130');
                var tmp = newwindow2.document;
                tmp.write('<html><head><title>Selected Document</title>');
                tmp.write('</head><body>');
                tmp.write('<p>'+text+'</p>');
                tmp.write('</body></html>');
                tmp.close();

            }
        </script>

        <script>
            function showHide()
            {
                if(document.getElementById("synsList").style.display == 'none') document.getElementById("synsList").style.display = 'block';
                else document.getElementById("synsList").style.display = 'none';
            }

        </script>

        <style>
            form select{
                float: right;
                color: red;
            }

            body{
                margin-left: 60px;
                margin-right: 60px;
                margin-bottom: 90px;
                margin-top: 60px;
                border: 4px solid orange;
                -moz-border-radius:15px;
                border-radius:15px;
            }

        </style>

    </head>

    <%! int index;%>


    <body>

        <p align="center">
            <img  src="images/aueb.png">
        </p>
        <p align="center">
            <i><strong>Athens University of Economics and Business</strong></i><br>
            <i><strong>Department of Computer Science</strong></i><br>
            <i><strong>Information Retrieval Systems</strong></i><br>
            <i><strong>Winter Semester 2012-2013</strong></i>
        </p>

        <form name="theForm" action="./Process" method="post" onsubmit="return validate()">

            <table style="border: 4px solid orange; -moz-border-radius:15px;
                   border-radius:15px;" bgcolor="orange" align="center">
                <tr>
                    <td align="left" style="width: 60">
                        <strong>Automatic query expansion</strong>
                        <input type="radio" name="autoExpChoice" value="1">
                    </td>
                    <td align="left" style="width: 60">
                        <strong>Term Selection</strong>
                        <input type="radio" name="autoExpChoice" value="2">
                    </td >
                    <td align="left" style="width: 60">
                        <strong>Default</strong>
                        <input type="radio" name="autoExpChoice" value="0" checked="checked">
                    </td>
                </tr>
                <tr>
                    <td align="left" style="width: 60">
                        <strong>Select k</strong>
                        <input type="text" style="width: 25px" name="kValue">
                    </td>
                    <td align="left" style="width: 60">
                        <p style="cursor: pointer" onclick="showHide()"> <font color="purple">Related Terms</font></p>

                    </td>
                    <td align="left" style="width: 60"></td>
                </tr>
            </table>

            <table align="center">
                <tr>
                    <td>
                        <%
                                    String value = (String) request.getAttribute("query");
                                    if (value == null) {
                                        value = "Type your query here...";
                                    }
                        %>
                        <input style=" border: 4px solid orange; -moz-border-radius:15px; border-radius:15px; width: 450px;"
                               type="text" id="queryText" name="queryText" value="<%=value%>">
                    </td>
                    <td><input style=" border: 4px solid;-moz-border-radius:15px; border-radius:15px;"
                               type="submit" name="searchButton" value="Go"></td>
                </tr>
            </table>






            <%
                        if (request.getAttribute("index") == null) {
                            index = -1;
                        } else {
                            index = (Integer) request.getAttribute("index");
                        }

                        List<List<String>> cutDocs = null;
                        List<List<String>> cutTitles = null;
                        List<String> titlesList = null;
                        List<String> docs = (List<String>) request.getAttribute("docs");
                        request.getSession().setAttribute("docs", docs);
                        List<String> titles = null;
                        int total = 0;
                        if (docs != null && index >= 0) {
                            titles = getTitles(docs);
                            cutTitles = createLists(titles);
                            cutDocs = createLists(docs);
            %>

            <p align="center">page <%=index + 1%> of <%=cutDocs.size()%></p>
            <p align="center"><%=docs.size()%> results returned</p>

            <%
                            out.println(createTable(cutDocs.get(index), cutTitles.get(index)));
                        }
            %>


            <table align="center">
                <tr>
                    <td align="left">
                        <strong>Previous</strong>
                        <input type="radio" name="action" value="prev">
                    </td>

                    <td align="center">
                        <strong>Next</strong>
                        <input type="radio" name="action" value="next">

                    </td>

                    <td align="right">
                        <strong>New Query</strong>
                        <input type="radio" name="action" value="query" checked="checked">
                    </td>
                </tr>
            </table>

        </form>


        <%
                    String synonyms = (String) request.getAttribute("synonyms");
                    List syns = null;
                    if (synonyms != null) {
                        syns = getSynonyms(synonyms);
                    }
        %>

        <form name="synsForm" action="">
            <select name="synsList" style="display:none" id="synsList" multiple="multiple" ondblclick="addSynonym(this.form)">
                <%
                            if (syns != null) {
                                for (int i = 0; i < syns.size(); i++) {
                                    out.println("<option>" + syns.get(i) + "</option>");
                                }
                            }
                %>
            </select>
        </form>




        <%!
           
            public String createTable(List<String> list, List<String> titles) {
                String ret = "";

                ret += "<table style=\"border:4px solid orange;-moz-border-radius:15px; border-radius:15px; \" align=\"center\">";
                for (int i = 0; i < list.size(); i++) {
                    String abbr = "abbr=" + "\"" + list.get(i) + "\"" + " onclick=" + "\"showDoc(this)" + "\"";
                    ret += "<tr><td " + abbr + ">" + "<a href=\"#\">" + titles.get(i) + "</a></td></tr>";
                }
                ret += "</table>";

                return ret;
            }

            public List<String> getTitles(List<String> docsIn) {
                List<String> docs = new ArrayList<String>(docsIn);
                List<String> titles = new ArrayList<String>();
                for (String document : docs) {
                    String title = "";
                    char c;
                    for (int i = 0; i < 80; i++) {
                        c = document.charAt(i);
                        //if(c != '.'){
                        title += c;
                        //}
                    }
                    titles.add(title + "..."); //take the first 80 characters as a document's title
                }
                return titles;
            }

            public List<String> getSynonyms(String synonyms) {
                ArrayList<String> list = new ArrayList<String>();
                String buffer = "";
                for (int i = 0; i < synonyms.length(); i++) {
                    if (synonyms.charAt(i) == ';') {
                        list.add(buffer);
                        buffer = "";
                    } else {
                        buffer += synonyms.charAt(i);
                    }
                }
                list.add(buffer);
                return list;
            }

            public List<List<String>> createLists(List<String> l) {
                List<List<String>> result = new ArrayList<List<String>>();
                if (l.size() <= 5) {
                    result.add(l);
                } else {
                    List<String> toAdd = null;
                    int count = 0;
                    for (int i = 0; i < l.size(); i++) {
                        if (count < 5) {
                            if (toAdd == null) {
                                toAdd = new ArrayList<String>();
                            }
                            toAdd.add(l.get(i));
                            ++count;
                        }
                        if (count == 5) {
                            result.add(toAdd);
                            count = 0;
                            toAdd = null;
                        }
                    }
                    result.add(toAdd);
                }

                return result;
            }

        %>
    </body>
</html>