package main;

import utils.DictionaryNode;
import utils.GetUserInput;
import utils.WordUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrosswordSolver {
	public static void main(String[] args) {
		// Get input
		String unknown = GetUserInput.getString("Enter the word, with '_' for unknown letters. ").toLowerCase();
		System.out.println();

		// Create list of all possibilities for each position
		List<Set<Character>> letters = new ArrayList<>();
		for (int index = 0; index < unknown.length(); index++) {
			char c = unknown.charAt(index);
			if (c == '_') {
				letters.add(allCharacterSet());
			} else if (c >= 'a' && c <= 'z') {
				Set<Character> letter = new HashSet<>();
				letter.add(c);
				letters.add(letter);
			} else if (c == '[') {
				// Move to first element inside square bracket
				index++;

				Set<Character> blank = new HashSet<>();
				boolean negated = false;

				// If first character is a ^, initialize blank as all characters and set negated flag, then move to next element
				if (unknown.charAt(index) == '^') {
					negated = true;
					blank = allCharacterSet();
					index++;
				}

				// Add or subtract each element of the square bracket
				for (c = unknown.charAt(index); c != ']'; index++, c = unknown.charAt(index)) {
					if (c >= 'a' && c <= 'z') {
						if (negated) blank.remove(c);
						else blank.add(c);
					} else {
						throw new IllegalArgumentException("Unexpected character: " + c + " (index " + index + ").");
					}
				}
				letters.add(blank);
			} else {
				throw new IllegalArgumentException("Unexpected character: " + c + " (index " + index + ").");
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

	private static List<String> possibleWords(List<Set<Character>> letters, DictionaryNode currentNode) {
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
	private static Set<Character> allCharacterSet() {
		return new HashSet<>() {{
			for (char c = 'a'; c <= 'z'; c++) {
				this.add(c);
			}
		}};
	}
}
