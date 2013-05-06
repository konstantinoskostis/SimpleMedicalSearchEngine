package Model;

import java.util.ArrayList;
import java.util.List;

public class Term {

	private int id;
	private String def;
	private String name;
	private List<String> synonymsList;
	private String is_a;
	
	public Term(){
		id = -1;
		def = " ";
		name = " ";
		synonymsList = new ArrayList<String>();
		is_a = " ";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDef() {
		return def;
	}

	public void setDef(String def) {
		this.def = def;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getList(){
		removeMultiples(synonymsList);
		return synonymsList;
	}
	
	public void addSynonym(String synonym){
		synonymsList.add(synonym);
	}
	
	public String getIs_a() {
		return is_a;
	}

	public void setIs_a(String is_a) {
		this.is_a = is_a;
	}
	
	private void removeMultiples(List<String> synonyms){
		String str;
	
		for(int i=0; i<synonyms.size(); i++){
			str = synonyms.get(i);
			for(int j=i+1; j<synonyms.size(); j++){
				if(str.equals(synonyms.get(j))){
					synonyms.remove(j);
				}
			}			
		}
		
	}//removes multiple instances of a synonym
	
	//for testing
	public void printTerm(){
		System.out.println("-------------------------");
		System.out.println("id: "+ getId());
		System.out.println("name: "+ getName());
		System.out.println("def: "+ getDef());
		System.out.println("is_a: "+ getIs_a());
		for(int i=0; i<getList().size(); i++){
			System.out.println("Synonym: "+ getList().get(i));
		}
		System.out.println("-------------------------");
	}
	
}
