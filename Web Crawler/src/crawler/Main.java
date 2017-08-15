package crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String[] words = new String[] { "an", "a", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he",
				"in", "is", "it", "its", "of", "this", "on", "that", "the", "to", "was", "were", "will", "with", "i" };

		List<String> list = new ArrayList<>(Arrays.asList(words));

		Crawler crawler = new Crawler(50, list);
		System.out.println("Total " + crawler.getMain_Dictionary().size() + " words in the dictionary");
		Thesaurus thes = new Thesaurus();

		System.out.println("Enter query below: ");
		Scanner sc = new Scanner(System.in);
		String query = sc.nextLine();
		boolean isExpanded = false;

		while (!query.toLowerCase().equals("stop")) {
			// Remove stop words from query and stem
			Stemmer stem = new Stemmer();
			stem.stemming(query, list);
			List<String> query_words = new ArrayList<>(stem.getStemmed_words());

			// Implement the cosine similarity
			CosineSimilarity cs = new CosineSimilarity(query_words, crawler);

			// Determine if need query expansion when result < 3
			if (!isExpanded && cs.findTop6Docs().size() < 3) {
				for (String word : query.split(" ")) {
					if (thes.getThesaurus().containsKey(word)) {
						for (String alts : thes.getThesaurus().get(word)) {
							query += " " + alts;
						}
					}
				}
				// System.out.println(query);
				isExpanded = true;
				continue;
			} else if (isExpanded && cs.findTop6Docs().size() == 0) {
				System.out.println("Not found!");
				System.out.println();
			} else {
				for (Doc doc : cs.findTop6Docs()) {
					System.out.println("Score: " + cs.getCosine_score().get(doc));
					System.out.println("URL: " + doc.getUrl());
					System.out.println("title: " + doc.getTitle());
					System.out.println(doc.getSummary());
					System.out.println();
				}
			}
			
			System.out.println("Actual query: " + query_words + "\n");
			System.out.println("Enter query below: ");
			sc = new Scanner(System.in);
			query = sc.nextLine();
			isExpanded = false;
		}

	}

}
