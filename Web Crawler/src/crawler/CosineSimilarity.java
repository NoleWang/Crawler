package crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

public class CosineSimilarity {
	private Map<Doc, List<Double>> w_tf_matrix;
	private Map<Doc, List<Double>> tf_idf_matrix;
	private Map<Doc, List<Double>> tf_cosine_matrix;
	private List<Double> idf_vector;
	private int[] query_frequency;
	private List<Double> w_query_vector;
	private List<Double> query_cosine;
	private Map<Doc, Double> cosine_score;

	public Map<Doc, Double> getCosine_score() {
		return cosine_score;
	}

	public CosineSimilarity(List<String> query, Crawler crawler) {
		w_tf_matrix = new HashMap<>();
		idf_vector = new ArrayList<>();
		tf_idf_matrix = new HashMap<>();
		tf_cosine_matrix = new HashMap<>();
		List<String> dict = new ArrayList<>();
		dict.addAll(crawler.getMain_Dictionary());
		query_frequency = new int[dict.size()];
		Arrays.fill(query_frequency, 0);
		w_query_vector = new ArrayList<>();
		query_cosine = new ArrayList<>();
		cosine_score = new HashMap<>();

		// Initialize query vector
		for (String string : query) {
			if (dict.contains(string)) {
				int index = dict.indexOf(string);
				query_frequency[index] += 1;
			}
		}

		// 1 + log(q)
		for (int i = 0; i < query_frequency.length; i++) {
			if (query_frequency[i] != 0) {
				double val = 1 + Math.log10(query_frequency[i]);
				w_query_vector.add(val);
			} else
				w_query_vector.add(0.0);
		}

		// 1 + log(tf)
		for (Doc doc : crawler.getList_documents()) {
			List<Double> list = new ArrayList<>();
			for (String word : dict) {
				double w_tf = 0.0;
				if (doc.getTerms_frequency().containsKey(word)) {
					int tf = doc.getTerms_frequency().get(word);
					w_tf = 1 + Math.log10(tf);
				}

				list.add(w_tf);
			}

			w_tf_matrix.put(doc, list);
		}

		// Compute idf
		for (String word : dict) {
			int df = crawler.getDocument_frequency().get(word).size();
			double idf = Math.log10((double) crawler.getList_documents().size() / (double) df);
			idf_vector.add(idf);
		}

		// tf-idf
		for (Doc doc : w_tf_matrix.keySet()) {
			List<Double> list = new ArrayList<>();
			double denominator = 0.0;
			for (int i = 0; i < idf_vector.size(); i++) {
				double tf_idf = idf_vector.get(i) * w_tf_matrix.get(doc).get(i);
				denominator += tf_idf * tf_idf;
				list.add(tf_idf);
			}
			list.add(Math.sqrt(denominator));
			tf_idf_matrix.put(doc, list);
		}

		// q-idf
		double denominator = 0.0;
		List<Double> query_idf = new ArrayList<>();
		for (int i = 0; i < idf_vector.size(); i++) {
			double q_idf = idf_vector.get(i) * w_query_vector.get(i);
			denominator += q_idf * q_idf;
			query_idf.add(q_idf);
		}

		// Normalize query
		for (int i = 0; i < query_idf.size(); i++) {
			double val = query_idf.get(i) / Math.sqrt(denominator);
			query_cosine.add(val);
		}

		// Normalization Cosine
		for (Doc doc : tf_idf_matrix.keySet()) {
			List<Double> list = new ArrayList<>();
			for (int i = 0; i < idf_vector.size(); i++) {
				double n_tf_idf = tf_idf_matrix.get(doc).get(i)
						/ tf_idf_matrix.get(doc).get(tf_idf_matrix.get(doc).size() - 1);
				list.add(n_tf_idf);
			}
			tf_cosine_matrix.put(doc, list);
		}

		// final score
		for (Doc doc : tf_cosine_matrix.keySet()) {
			double score = 0.0;
			for (int i = 0; i < query_cosine.size(); i++) {
				score += tf_cosine_matrix.get(doc).get(i) * query_cosine.get(i);
			}

			if(iSQueryInTitle(query, doc)){
				score += 0.5;
			}

			// System.out.println(doc.getTitle() + " " + score);
			cosine_score.put(doc, score);
		}

	}

	private boolean iSQueryInTitle(List<String> query, Doc doc) {
		for (String string : query) {
			String[] title = doc.getTitle().toLowerCase().split("\\W+");
			Set<String> set = new HashSet<>(Arrays.asList(title));
			if (set.contains(string))
				return true;
		}

		return false;
	}

	public List<Doc> findTop6Docs() {
		// TODO Auto-generated method stub
		List<Doc> result = new ArrayList<>();
		Queue<Entry<Doc, Double>> q = new PriorityQueue<>(cosine_score.size(), new Comparator<Entry<Doc, Double>>() {
			public int compare(Entry<Doc, Double> n1, Entry<Doc, Double> n2) {
				if (n1.getValue() < n2.getValue())
					return 1;
				else if (n1.getValue() > n2.getValue())
					return -1;
				else
					return 0;
			}
		});

		for (Entry<Doc, Double> entry : cosine_score.entrySet()) {
			q.add(entry);
		}

		for (int i = 0; i < 6; i++) {
			double score = q.peek().getValue();
			if (score > 0) {
				Doc doc = q.poll().getKey();
				result.add(doc);
			} else
				break;
		}

		return result;
	}
}
