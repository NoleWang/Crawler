package crawler;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parser {
	private Set<String> links;
	private Set<String> outgoingLinks;
	private Document document;
	private String contents;
	private Elements linksOnPage;
	
	public String getContents() {
		return contents;
	}

	public Set<String> getLinks() {
		for (Element link : linksOnPage) {
			//System.out.println(link.attr("href"));
			String url = link.absUrl("href");
			if(url.startsWith("http://lyle.smu.edu/~fmoore")){			
				links.add(url);
			}else
				outgoingLinks.add(url);
		}
		
		return links;
	}

	public Set<String> getOutgoingLinks() {
		return outgoingLinks;
	}
	
	public Parser(Document page){
		links = new HashSet<String>();
		outgoingLinks = new HashSet<String>();
		document = page;	
		contents = document.body().text();
		linksOnPage = document.select("a[href]");
	}
	
}
