package wordsolvers.main;

import wordsolvers.structs.BlankSpace;
import wordsolvers.structs.DictionaryNode;
import wordsolvers.utils.GetUserInput;
import wordsolvers.utils.WordUtils;

import java.util.*;

public class CrosswordSolver {
	public static void main(String[] args) {
		// Get input
		String unknown = GetUserInput.getString("Enter the word, with '_' for unknown letters. ").toLowerCase();
		System.out.println();

		List<BlankSpace> spaces = new ArrayList<>();
		int minLength = 0, maxLength = Integer.MAX_VALUE;

		// Find possible words
		Collection<String> words = possibleWords(spaces, WordUtils.getDictTree(), minLength, maxLength);

		// Print out possibilities
		if (words.size() == 0) {
			System.out.println("Sorry, no words found.");
		} else {
			for (String word : words) {
				System.out.println(word);
			}
		}
	}

	private static int getClosingBracketIndex(String unknown, int start) {
		// Determine bracket type
		char c = unknown.charAt(start);
		boolean filter = c == '{' || c == '(';
		char endBracket = c == '{' ? '}' : (c == '(' ? ')' : ']');

		if (endBracket == ']' && c != '[') {
			throw new IllegalArgumentException("Given index (" + start + ") is not a bracket");
		}

		// Get substring of inside of bracket
		int startIndex = start + 1, endIndex = startIndex;
		for (c = unknown.charAt(endIndex); c != endBracket; endIndex++, c = unknown.charAt(endIndex)) {
			if (endIndex == unknown.length() - 1) {
				throw new IllegalArgumentException("No closing brackets for bracket at index " + start + ".");
			}
		}
		return endIndex;
	}

	private static Collection<String> possibleWords(List<BlankSpace> letters, DictionaryNode currentNode, int minLetters, int maxLetters) {
		Collection<String> ret = new LinkedHashSet<>(); // Preserve order (roughly alphabetical) but prevent repeats

		// Ensure bounds are kept
		int len = currentNode.length();
		int[] bounds = getLengthBounds(letters);
		int minLen = len + bounds[0], maxLen = addWithOverflow(len, bounds[1]);
		if (minLen > maxLetters || maxLen < minLetters) {
			return ret;
		}

		// If no more letters, return list containing current word, or empty list if currentNode is not a word
		if (letters.size() == 0) {
			if (currentNode.isWord()) ret.add(currentNode.toString());
			return ret;
		}

		BlankSpace currentSpace = letters.get(0);
		// Pull out current BlankSpace
		List<BlankSpace> skipFirst = letters.subList(1, letters.size());

		// If the minimum is 0, add all the possibilities with the current completely skipped
		if (currentSpace.minBlanks == 0) {
			ret.addAll(possibleWords(skipFirst, currentNode, minLetters, maxLetters));
		}
		// If the max is also 0, return; otherwise, continue
		if (currentSpace.maxBlanks == 0) {
			return ret;
		}

		// letters but with one less possible space in currentSpace
		List<BlankSpace> newLetters = new ArrayList<>();
		newLetters.add(currentSpace.subtractBlank());
		newLetters.addAll(skipFirst);

		// Look the next character down the tree. If exists, go there and recurse with one less space in current node
		for (char c : currentSpace.possibleCharacters) {
			if (currentNode.hasChild(c)) {
				ret.addAll(possibleWords(newLetters, currentNode.getChild(c), minLetters, maxLetters));
			}
		}

		return ret;
	}

	// Returns a 2 element array containing the minimum and maximum word length
	private static int[] getLengthBounds(List<BlankSpace> spaces) {
		int min = 0, max = 0;
		for (BlankSpace space : spaces) {
			min += space.minBlanks;
			max = addWithOverflow(max, space.maxBlanks);
		}
		return new int[] { min, max };
	}

	private static int addWithOverflow(int a, int b) {
		long sum = ((long) a) + ((long) b);
		if (sum > (long)Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int)sum;
	}
}
