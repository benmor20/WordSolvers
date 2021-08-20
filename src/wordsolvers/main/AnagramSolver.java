package wordsolvers.main;

import wordsolvers.structs.DictionaryNode;
import wordsolvers.structs.UnknownWord;
import wordsolvers.structs.parsers.UnknownWordParser;
import wordsolvers.utils.GetUserInput;
import wordsolvers.utils.WordUtils;

import java.util.*;

public class AnagramSolver {
	private static final int MAX_RESULTS = 50;
	private static final int MAX_WORDS = 3;

	public static void main(String[] args) {
		String anagram = GetUserInput.getString("Enter the anagram. ");
		UnknownWord word = new UnknownWordParser().parse(anagram);

		Collection<String> results = word.possibleAnagrams(WordUtils.getDictTree(), MAX_WORDS);
		List<String> trueRes = new ArrayList<>();

		System.out.println("Got Results (" + results.size() + "). Sorting...");
		if (results.size() > MAX_RESULTS) {
			Map<String, Double> scores = new HashMap<>();
			double bestScore = Integer.MAX_VALUE;
			String bestPhrase = "";
			char start = 0;
			for (String res : results) {
				double score = WordUtils.scorePhrase(res);
				if (score < bestScore) {
					bestScore = score;
					bestPhrase = res;
				}
				scores.put(res, score);
				if (res.charAt(0) != start) {
					System.out.println("Starting score of " + res.charAt(0));
					start = res.charAt(0);
				}
			}
			System.out.println("Scored phrases");
			results.remove(bestPhrase);
			trueRes.add(bestPhrase);
			System.out.println("Decided 1");
			for (int topScore = 1; topScore < MAX_RESULTS; topScore++) {
				bestScore = Integer.MAX_VALUE;
				bestPhrase = "";
				for (String res : results) {
					double score = scores.get(res);
					if (score < bestScore) {
						bestScore = score;
						bestPhrase = res;
					}
				}
				results.remove(bestPhrase);
				trueRes.add(bestPhrase);
				System.out.println("Decided " + (topScore + 1));
			}
		} else {
			trueRes.addAll(results);
			trueRes.sort(Comparator.comparingDouble(WordUtils::scorePhrase));
		}
		//results.sort(Comparator.comparingInt(WordUtils::scorePhrase));
		System.out.println("Results:");
		for (String res : trueRes) {
			System.out.println(res);
		}
	}
}
