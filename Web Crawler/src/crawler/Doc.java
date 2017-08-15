package crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Doc {
	private String url;
	private String contents;
	private String title;
	private StringBuilder summary;
	private Map<String, Integer> terms_frequency;
	private Stemmer stemmer;
	private List<String> stemmed_words;
	//private Set<String> words_list;

	public Doc(String contents, List<String> stopwords){
		this.contents = contents;
		terms_frequency = new HashMap<>();
		summary = new StringBuilder();
		stemmer = new Stemmer();
		stemmer.stemming(contents, stopwords);
		stemmed_words = new ArrayList<>(stemmer.getStemmed_words());
		
		int count = 1;
		for(String word : stemmed_words){
			terms_frequency.putIfAbsent(word, 0);
			terms_frequency.put(word, terms_frequency.get(word) + 1);
			if(count <= 20){
				summary.append(word + " ");
				count++;
			}else
				continue;
		}
		
		//System.out.println(terms_frequency);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getContents() {
		return contents;
	}
	
	public Map<String, Integer> getTerms_frequency() {
		return terms_frequency;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public StringBuilder getSummary() {
		return summary;
	}
	
}
