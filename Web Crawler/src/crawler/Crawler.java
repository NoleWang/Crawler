package crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import robotstxt.RuleParser;

public class Crawler {

	private Set<String> urlsVisited;
	private Set<String> pagesVisited;
	private Set<String> brokenLinks;
	private Set<String> fetchedLinks;
	private Map<String, List<String>> ruleSet;
	private Queue<String> urlFrontier;
	private Set<String> outgoingLinks;
	private String currentUrl;
	private Parser content;
	private Document document;
	private int count;
	private final String root = "http://lyle.smu.edu/~fmoore/";
	private final String userAgent = "Crawler";
	private RuleParser rr;
	private List<Doc> list_documents;
	private Set<String> main_Dictionary;
	private Map<String, Integer> word_frequency;
	private Map<String, List<Integer>> document_frequency; 
	
	public Set<String> getMain_Dictionary() {
		return main_Dictionary;
	}
	
	public Map<String, List<Integer>> getDocument_frequency() {
		return document_frequency;
	}

	public List<Doc> getList_documents() {
		return list_documents;
	}
	
	public Crawler(int maxPages_to_Retrieve, List<String> stopwords) {
		currentUrl = root;
		urlFrontier = new LinkedList<String>();
		urlsVisited = new HashSet<String>();
		outgoingLinks = new HashSet<String>();
		pagesVisited = new HashSet<String>();
		brokenLinks = new HashSet<String>();
		urlFrontier.add(currentUrl);
		count = 0;
		rr = new RuleParser(root);
		ruleSet = rr.getRuleSet();
		list_documents = new ArrayList<>();
		main_Dictionary = new HashSet<>();
		document_frequency = new HashMap<>();
		//System.out.println(ruleSet);

		while (!urlFrontier.isEmpty() && pagesVisited.size() < maxPages_to_Retrieve) {
			// Fetch
			currentUrl = urlFrontier.poll();
			if(currentUrl.contains(".pdf"))
				continue;
			
			Connection page = null;
			try {
				page = Jsoup.connect(currentUrl).userAgent(userAgent);
				document = page.get();

				// System.out.println("\n**Visiting** Received web page at " +
				// currentUrl);
			} catch (UnsupportedMimeTypeException e) {

				if (currentUrl.contains(".gif") || currentUrl.contains(".jpg") 
						|| currentUrl.contains(".png")) {
					count++;
				}
				continue;

			} catch (Exception e) {
				//e.printStackTrace();
				
				if (page.response().statusCode() >= 400 
						|| page.response().statusCode() == 0) {
					// System.out.println("Bad link!");
					brokenLinks.add(currentUrl);
					continue;
				}
			}

			// Parse
			if (document != null)
				content = new Parser(document);

			// Content seen?
			if (!contentSeen(content.getContents(), pagesVisited)) {
				//System.out.println(document.getElementsByTag("title").text());
				urlsVisited.add(currentUrl);
				pagesVisited.add(content.getContents());
				Doc doc = new Doc(content.getContents(), stopwords);
				doc.setUrl(currentUrl);
				Stemmer stem = new Stemmer();
				stem.stemming(document.getElementsByTag("title").text(), stopwords);
				ArrayList<String> list = new ArrayList<>(stem.getStemmed_words());
				String title = String.join(" ", list);
				doc.setTitle(title);
				list_documents.add(doc);
			} else {
				//System.out.println("Page: " + currentUrl + " is seen content");
				continue;
			}
			
			fetchedLinks = new HashSet<>(content.getLinks());
			// URL filter: check robots templates to see whether links are allowed
			for (String string : content.getLinks()) {
				if (!urlFilter(string, ruleSet, userAgent))
					fetchedLinks.remove(string);
			}
			
			// Remove duplicate URLs
			for (String string : fetchedLinks) {
				if (!urlsVisited.contains(string))
					urlFrontier.add(string);
				/*else
					System.out.println(string + " is a duplicate");*/
			}

			outgoingLinks.addAll(content.getOutgoingLinks());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

/*		System.out.println("Page Link: " + list_documents.size());
		for (Doc doc : list_documents) {
			System.out.println(doc.getUrl() + " ");
			System.out.println("title: " + doc.getTitle());
			System.out.println(doc.getSummary().toString());
		}*/
		
/*		System.out.println("OutgoingLink: " + outgoingLinks.size());
		for (String string : outgoingLinks) {
			System.out.println(string);
		}
		
		System.out.println("BrokenLink: " + brokenLinks.size());
		for (String string : brokenLinks) {
			System.out.println(string);
		}

		System.out.println("# of graphic files: " + count);
		
		//print out all documents
		for (int i = 0; i < list_documents.size(); i++) {
			System.out.println("Doc " + (i + 1) + " from " + list_documents.get(i).getUrl());
		}*/
		
		//Build a complete dictionary
		for (Doc doc : list_documents) {
			main_Dictionary.addAll(doc.getTerms_frequency().keySet());
		}
		
		//Merge all term frequency from separate documents
		word_frequency = new HashMap<>(list_documents.get(0).getTerms_frequency());
		for(int i = 1; i < list_documents.size(); i++){
			for (String word : list_documents.get(i).getTerms_frequency().keySet()) {
				word_frequency.putIfAbsent(word, 0);
				word_frequency.put(word, word_frequency.get(word) 
						+ list_documents.get(i).getTerms_frequency().get(word));
			}
		}
		
		//Store document frequency
		for (String string : main_Dictionary) {
			document_frequency.putIfAbsent(string, new ArrayList<Integer>());
			for (int i = 0; i < list_documents.size(); i++) {
				if(list_documents.get(i).getTerms_frequency().containsKey(string))
					document_frequency.get(string).add(i);
				
			}	
		}
		
/*		String[] top_words = findTop20Words();
		for (int i = 0; i < top_words.length; i++) {
			System.out.println((i+1) + " " + top_words[i] 
					+ " tf= " + word_frequency.get(top_words[i]) + " df= " + document_frequency.get(top_words[i]).size());
		}*/
	}

	private String[] findTop20Words() {
		// TODO Auto-generated method stub
		Queue<Entry<String, Integer>> q = new PriorityQueue<>(word_frequency.size(), new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> n1, Entry<String, Integer> n2) {
				if (n1.getValue() < n2.getValue())
					return 1;
				else if (n1.getValue() > n2.getValue())
					return -1;
				else
					return 0;
			}
		});
		
		for (Entry<String, Integer> entry : word_frequency.entrySet()) {
			q.add(entry);
		}
		
		String[] top_words = new String[20];
		for (int i = 0; i < top_words.length; i++) {
			top_words[i] = q.poll().getKey();
		}

		return top_words;
	}

	public boolean contentSeen(String contents, Set<String> pagesVisited) {
		// System.out.println("Current contents: " + contents);
		for (String content : pagesVisited) {
			if (content.equals(contents))
				return true;
		}

		return false;
	}

	public boolean urlFilter(String path, Map<String, List<String>> map, 
			String userAgent) {
		URL url = null;
		try {
			url = new URL(path);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(map.containsKey(userAgent)){
			for (String rule : map.get(userAgent)) {
				if (!urlChecking(url, rule))
					return false;
			}
		}
	
		for (String rule : map.get("*")) {
			if (!urlChecking(url, rule))
				return false;
		}

		return true;
	}

	private boolean urlChecking(URL url, String rule) {
		String path = url.getPath();
		//System.out.println(path);
		if (rule.length() == 0)
			return true; // allows everything if BLANK

		if (rule == "/")
			return false; // allows nothing if /

		if (path.contains(rule))
			return false;

		return true;
	}

}
