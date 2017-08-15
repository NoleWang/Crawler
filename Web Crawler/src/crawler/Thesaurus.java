package crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Thesaurus {
	private Map<String, List<String>> thesaurus;
	
	public Map<String, List<String>> getThesaurus() {
		return thesaurus;
	}

	public Thesaurus(){
		thesaurus = new HashMap<>();
		File file = new File("src/thesaurus.txt");
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String string = br.readLine();
			while(string != null && !string.isEmpty()){
				String[] terms = string.split(" ");
				List<String> alternates = new ArrayList<>();
				for (int i = 1; i < terms.length; i++) {
					if(!terms[i].trim().isEmpty())
						alternates.add(terms[i].trim());
				}
				thesaurus.put(terms[0], alternates);
				string = br.readLine();
			}
			
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(thesaurus);
	}
}
