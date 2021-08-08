package wordsolvers.main;

import wordsolvers.utils.BlankSpace;
import wordsolvers.utils.DictionaryNode;
import wordsolvers.utils.GetUserInput;
import wordsolvers.utils.WordUtils;

import java.util.*;

public class CrosswordSolver {
	public static void main(String[] args) {
		// Get input
		String unknown = GetUserInput.getString("Enter the word, with '_' for unknown letters. ").toLowerCase();
		System.out.println();

		// Create list of all possibilities for each position
		List<BlankSpace> spaces = new ArrayList<>();
		int minLength = 0, maxLength = Integer.MAX_VALUE;
		for (int index = 0; index < unknown.length(); index++) {
			char c = unknown.charAt(index);
			if (c == '_') {
				spaces.add(new BlankSpace(false));
			} else if (c >= 'a' && c <= 'z') {
				spaces.add(new BlankSpace(false, c));
			} else if (c == '[') {
				spaces.add(getBracket(unknown, index));
				index = getClosingBracketIndex(unknown, index);
			} else if (c == '{') {
				// Apply filter
				BlankSpace filter = getBracket(unknown, index);
				for (BlankSpace space : spaces) {
					space.applyFilter(filter.possibleCharacters, true);
				}
				minLength = filter.minBlanks;
				maxLength = filter.maxBlanks;

				// Can only make it this far if at end of input
				int endIndex = getClosingBracketIndex(unknown, index);
				if (endIndex < unknown.length() - 1) {
					throw new IllegalArgumentException("Symbols found after filter: " + unknown.substring(endIndex + 1));
				}
				break; // if above is true, have searched full input
			} else if (c == '(') {
				// Apply filter
				BlankSpace filter = getBracket(unknown, index);
				for (BlankSpace space : spaces) {
					space.applyFilter(filter.possibleCharacters, false);
				}
				minLength = filter.minBlanks;
				maxLength = filter.maxBlanks;

				// Can only make it this far if at end of input
				int endIndex = getClosingBracketIndex(unknown, index);
				if (endIndex < unknown.length() - 1) {
					throw new IllegalArgumentException("Symbols found after filter: " + unknown.substring(endIndex + 1));
				}
				break; // if above is true, have searched full input
			} else {
				throw new IllegalArgumentException("Unexpected character: " + c + " (index " + index + ").");
			}
		}

		for (int space = 0; space < spaces.size(); space++) {
			if (spaces.get(space).possibleCharacters.size() == 0) {
				throw new IllegalArgumentException("Filter clears all possibilities from space " + (space + 1));
			}
		}

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

	private static BlankSpace getBracket(String unknown, int start) {
		// Get inside bracket
		String inner = unknown.substring(start + 1, getClosingBracketIndex(unknown, start));

		// If nothing inside brackets, throw exception
		if (inner.length() == 0) throw new IllegalArgumentException("Empty brackets (index " + start + ").");

		int innerIndex = 0;
		char c = inner.charAt(innerIndex);
		boolean isFilter = unknown.charAt(start) != '['; // Must be '(' or '{' - getClosingBracket would have thrown an exception otherwise
		int min = 1, max = isFilter ? Integer.MAX_VALUE : 1; // Default bounds - (1,MAX) if filter, (1,1) if not
		if (c >= '0' && c <= '9') { // Set minumum bounds
			min = 0;
			for (; c >= '0' && c <= '9'; innerIndex++, c = inner.charAt(innerIndex)) {
				min *= 10;
				min += c - '0';
				if (innerIndex == inner.length() - 1) {
					throw new IllegalArgumentException("Missing comma at index " + (start + innerIndex + 1) + ".");
				}
			}
			if (c != ',') throw new IllegalArgumentException("Missing comma at index " + (start + innerIndex + 1) + ".");
		}
		if (c == ',') { // Either has just min, just max, or both
			// No minimum bound (did not go into previous if statement)
			if (innerIndex == 0) {
				min = 0;
			}

			// Skip comma
			innerIndex++;
			if (innerIndex < inner.length()) {
				c = inner.charAt(innerIndex);

				if (c >= '0' && c <= '9') { // Set maximum bounds
					max = 0;
					for (; c >= '0' && c <= '9'; innerIndex++, c = inner.charAt(innerIndex)) {
						max *= 10;
						max += c - '0';
						if (innerIndex == inner.length() - 1) {
							innerIndex++;
							break;
						}
					}
				} else { // No maximum
					max = Integer.MAX_VALUE;
				}
			} else { // No maximum or limits
				max = Integer.MAX_VALUE;
			}
		}

		if (min > max) throw new IllegalArgumentException("Lower bound cannot be more than upper bound (min: " + min + " max: " + max + "index: " + start + ")");

		if (innerIndex < inner.length()) { // Letter limits given
			boolean negated = false;
			if (c == '^') { // One space, find negation
				negated = true;
				innerIndex++;
			}

			if (innerIndex < inner.length()) {
				BlankSpace ret = new BlankSpace(true, min, max, negated);
				c = inner.charAt(innerIndex);
				if (c >= 'a' && c <= 'z') {
					for (; c >= 'a' && c <= 'z'; innerIndex++, c = inner.charAt(innerIndex)) {
						ret.appendLetter(c);
						if (innerIndex == inner.length() - 1) {
							break;
						}
					}
				} else {
					throw new IllegalArgumentException("Unexpected character: " + c + " (index " + (start + innerIndex + 1) + ").");
				}
				return ret;
			} else { // ^ ends brackets
				return new BlankSpace(true, min, max, true);
			}
		} else { // Only bounds given
			return new BlankSpace(true, min, max, false, WordUtils.allCharacterSet());
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
