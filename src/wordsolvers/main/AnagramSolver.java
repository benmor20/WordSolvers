package wordsolvers.main;

import wordsolvers.structs.DictionaryNode;
import wordsolvers.utils.GetUserInput;
import wordsolvers.utils.WordUtils;

import java.util.*;

public class AnagramSolver {
	private static final int MAX_RESULTS = 20;
	private static final int MAX_WORDS = 3;

	public static void main(String[] args) {
		String anagram = GetUserInput.getString("Enter the anagram. ");

		List<String> results = solveAnagram(anagram, MAX_WORDS);
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

	public static List<String> solveAnagram(String anagram, int maxWords) {
		return solveAnagram(anagram, maxWords, WordUtils.getDictTree(), true);
	}
	private static List<String> solveAnagram(String anagram, int maxWords, DictionaryNode currentNode, boolean first) {
		if (anagram.length() == 0) {
			return new ArrayList<>() {{
				this.add("");
			}};
		}
		List<String> solved = new ArrayList<>();
		Set<Character> alreadyChecked = new HashSet<>();
		for (char c : anagram.toCharArray()) {
			if (alreadyChecked.contains(c)) continue;
			alreadyChecked.add(c);
			if (!currentNode.hasChild(c)) continue;
			List<String> nextLayer = solveAnagram(anagram.replaceFirst(c + "", ""), maxWords, currentNode.getChild(c), false);
			for (String phrase : nextLayer) {
				solved.add(c + phrase);
			}
			if (first) System.out.println("Done with " + c);
		}
		if (maxWords > 1 && currentNode.isWord()) {
			if (first) System.out.println("Node: " + currentNode + " Adding space...");
			List<String> nextLayer = solveAnagram(anagram, maxWords - 1, WordUtils.getDictTree(), false);
			for (String phrase : nextLayer) {
				solved.add(" " + phrase);
			}
			if (first) System.out.println("Added space");
		}
		return solved;
	}

	public static List<String> possibleBrokenWords(String word, int maxWords) {
		if (maxWords == 1) {
			if (WordUtils.isWord(word)) {
				return new ArrayList<>() {{
					this.add(word);
				}};
			}
			return new ArrayList<>();
		}

		List<String> ret = new ArrayList<>();
		if (WordUtils.isWord(word)) ret.add(word);
		for (int spaceIndex = 1; spaceIndex < word.length(); spaceIndex++) {
			String first = word.substring(0, spaceIndex),
					last = word.substring(spaceIndex);
			if (!WordUtils.isWord(first)) continue;
			List<String> nextRes = possibleBrokenWords(last, maxWords - 1);
			for (String res : nextRes) {
				ret.add(first + " " + res);
			}
		}
		return ret;
	}
}
