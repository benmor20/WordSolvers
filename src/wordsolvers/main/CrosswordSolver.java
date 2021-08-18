package wordsolvers.main;

import wordsolvers.structs.BlankSpace;
import wordsolvers.structs.DictionaryNode;
import wordsolvers.structs.UnknownWord;
import wordsolvers.structs.parsers.UnknownWordParser;
import wordsolvers.utils.GetUserInput;
import wordsolvers.utils.WordUtils;

import java.util.*;

public class CrosswordSolver {
	public static void main(String[] args) {
		// Get input
		String unknown = GetUserInput.getString("Enter the word, with '_' for unknown letters. ").toLowerCase();
		System.out.println();

		// Find possible words
		UnknownWord word = new UnknownWordParser().parse(unknown);
		Collection<String> words = word.possibleWords(WordUtils.getDictTree());

		// Print out possibilities
		if (words.size() == 0) {
			System.out.println("Sorry, no words found.");
		} else {
			for (String w : words) {
				System.out.println(w);
			}
		}
	}
}
