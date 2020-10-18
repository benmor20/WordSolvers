package main;

import utils.DictionaryNode;
import utils.GetUserInput;
import utils.WordUtils;

import java.util.ArrayList;
import java.util.List;

public class CrosswordSolver {
	public static void main(String[] args) {
		// Get input
		String unknown = GetUserInput.getString("Enter the word, with '_' for unknown letters. ");
		System.out.println();

		// Create list of all possibilities for each position
		List<List<Character>> letters = new ArrayList<>();
		for (char c : unknown.toLowerCase().toCharArray()) {
			if (c == '_') {
				letters.add(allCharacterList());
			} else if (c >= 'a' && c <= 'z') {
				letters.add(new ArrayList<>() {{
					this.add(c);
				}});
			} else {
				throw new IllegalArgumentException("Unknown character: " + c);
			}
		}

		// Find possible words
		List<String> words = possibleWords(letters, WordUtils.getDictTree());

		// Print out possibilities
		if (words.size() == 0) {
			System.out.println("Sorry, no words found.");
		} else {
			for (String word : words) {
				System.out.println(word);
			}
		}
	}

	private static List<String> possibleWords(List<List<Character>> letters, DictionaryNode currentNode) {
		List<String> ret = new ArrayList<>();

		// If no more letters, return list containing current word, or empty list if currentNode is not a word
		if (letters.size() == 0) {
			if (currentNode.isWord()) ret.add(currentNode.toString());
			return ret;
		}

		// Otherwise, loop through possible letters and add the possibilities from the next layer down
		for (char c : letters.get(0)) {
			if (currentNode.hasChild(c)) {
				ret.addAll(possibleWords(letters.subList(1, letters.size()), currentNode.getChild(c)));
			}
		}
		return ret;
	}

	// Returns a list containing each letter from a to z
	private static List<Character> allCharacterList() {
		return new ArrayList<>() {{
			for (char c = 'a'; c <= 'z'; c++) {
				this.add(c);
			}
		}};
	}
}
