package wordsolvers.main;

import wordsolvers.structs.DictionaryNode;
import wordsolvers.structs.UnknownWord;
import wordsolvers.structs.parsers.UnknownWordParser;
import wordsolvers.utils.GetUserInput;
import wordsolvers.utils.WordUtils;

import java.util.*;

public class AnagramSolver {
	private static final int MAX_RESULTS = 20;
	private static final int MAX_WORDS = 3;

	public static void main(String[] args) {
		String anagram = GetUserInput.getString("Enter the anagram. ");
		UnknownWord word = new UnknownWordParser().parse(anagram);

		System.out.println("Finding anagrams...");
		List<String> results = new ArrayList<>(word.possibleAnagrams(WordUtils.getDictTree(), MAX_WORDS));
		System.out.println("Got " + results.size() + " results.");
		System.out.println("Mapping...");
		Map<String, Double> toScores = WordUtils.mapToScores(results);
		System.out.println("Sorting...");
		results.sort(Comparator.comparingDouble(toScores::get));

		System.out.println("Results:");
		int index = 0;
		while (true) {
			for (String res : results.subList(index, Math.min(index + MAX_RESULTS, results.size()))) {
				System.out.println(res);
			}
			System.out.println();
			index += MAX_RESULTS;
			if (index > results.size()) {
				System.out.println("That's all!");
				break;
			}
			boolean more = GetUserInput.getBoolean("Would you like to see more? ");
			if (!more) break;
			System.out.println();
		}
	}
}
