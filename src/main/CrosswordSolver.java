package main;

import utils.DictionaryNode;
import utils.GetUserInput;
import utils.WordUtils;

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
					space.applyFilter(true, filter.possibleCharacters);
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
					space.applyFilter(false, filter.possibleCharacters);
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
		int min = 1, max = 1; // Default bounds
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
			return new BlankSpace(true, min, max, false, allCharacterSet());
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
		int minLen = len + bounds[0], maxLen = len + bounds[1];
		if (minLen > maxLetters || maxLen < minLetters) return ret;

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
			max += space.maxBlanks;
		}
		return new int[] { min, max };
	}

	// Returns a list containing each letter from a to z
	private static Set<Character> allCharacterSet() {
		return new HashSet<>() {{
			for (char c = 'a'; c <= 'z'; c++) {
				this.add(c);
			}
		}};
	}

	// Represents one element from the input (i.e. 'a', '[2,]', or '[0,4^abc]')
	private static class BlankSpace {
		// Possible characters for this blank space
		public final Set<Character> possibleCharacters;

		// When this is negated, keeps track of removed characters
		public final Set<Character> removedCharacters;

		// How many blanks this space can represent
		public final int minBlanks, maxBlanks;

		// Whether, when appending characters, to subtract from possibleCharacters (true) or to add (false)
		// Equivalently, whether there is a ^ operator
		private final boolean negated;

		// Whether this space was given as brackets ('a' vs '[a]', or '_' vs '[^]' or '[abc...xyz]'), which matters for filters)
		private final boolean bracketed;

		public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks, boolean negated, Collection<Character> cs) {
			this(bracketed, minBlanks, maxBlanks, negated);
			this.appendAll(cs);
		}
		public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks, boolean negated, char c) {
			this(bracketed, minBlanks, maxBlanks, negated);
			this.appendLetter(c);
		}
		public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks, boolean negated) {
			this.possibleCharacters = negated ? allCharacterSet() : new HashSet<>();
			this.removedCharacters = new HashSet<>();
			this.minBlanks = minBlanks;
			this.maxBlanks = maxBlanks;
			this.negated = negated;
			this.bracketed = bracketed;
		}
		public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks, Collection<Character> cs) {
			this(bracketed, minBlanks, maxBlanks);
			this.appendAll(cs);
		}
		public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks, char c) {
			this(bracketed, minBlanks, maxBlanks);
			this.appendLetter(c);
		}
		public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks) {
			this(bracketed, minBlanks, maxBlanks, false);
		}
		public BlankSpace(boolean bracketed, boolean negated, Collection<Character> cs) {
			this(negated, bracketed);
			this.appendAll(cs);
		}
		public BlankSpace(boolean bracketed, boolean negated, char c) {
			this(negated, bracketed);
			this.appendLetter(c);
		}
		public BlankSpace(boolean bracketed, boolean negated) {
			this(bracketed, 1, 1, negated);
		}
		public BlankSpace(boolean bracketed, char c) {
			this(bracketed, false);
			this.appendLetter(c);
		}
		public BlankSpace(boolean bracketed) {
			this(bracketed, false);
			if (!this.bracketed) { // Not bracketed with no specified character means '_'
				this.addAll(allCharacterSet());
			}
		}

		// Returns a BlankSpace with one less blank
		public BlankSpace subtractBlank() {
			if (this.minBlanks == 0 && this.maxBlanks == 0) throw new IllegalStateException("Cannot subtract BlankSpace with no spaces");
			BlankSpace ret = new BlankSpace(this.bracketed, (int)Math.max(this.minBlanks - 1, 0), this.maxBlanks - 1, this.negated);
			if (this.negated) {
				ret.possibleCharacters.removeAll(this.removedCharacters);
				ret.removedCharacters.addAll(this.removedCharacters);
			} else {
				ret.possibleCharacters.addAll(this.possibleCharacters);
			}
			return ret;
		}

		// Adds or subtracts the given character from the range of possibilities, depending on negation
		public void appendLetter(char c) {
			if (this.negated) {
				this.removeLetter(c);
			} else {
				this.addLetter(c);
			}
		}
		// Repeats appendLetter(c) for each element of cs
		public void appendAll(Collection<Character> cs) {
			if (this.negated) {
				this.removeAll(cs);
			} else {
				this.addAll(cs);
			}
		}

		// Add c to the list of possible characters
		public void addLetter(char c) {
			this.removedCharacters.remove(c);
			this.possibleCharacters.add(c);
		}
		// Repeats addLetter(c) for each element of cs
		public void addAll(Collection<Character> cs) {
			this.removedCharacters.removeAll(cs);
			this.possibleCharacters.addAll(cs);
		}

		// Remove c from the list of possible characters
		public void removeLetter(char c) {
			this.possibleCharacters.remove(c);
			this.removedCharacters.add(c);
		}
		// Repeats removeLetter(c) for each element of cs
		public void removeAll(Collection<Character> cs) {
			this.possibleCharacters.removeAll(cs);
			this.removedCharacters.addAll(cs);
		}

		// Applies a filter over all the letters
		public void applyFilter(boolean hardFilter, Collection<Character> filter) {
			// See README for the filter rules
			if ((hardFilter && (this.bracketed || this.possibleCharacters.size() > 1))
					|| (!hardFilter && !this.bracketed && this.possibleCharacters.size() > 1)) {
				Set<Character> toRemove = allCharacterSet();
				toRemove.removeAll(filter);
				this.removeAll(toRemove);
			}
		}

		// Returns whether this BlankSpace can represent a variable number of blanks (i.e. '[2,5]')
		public boolean hasVariableBlanks() {
			return this.minBlanks != this.maxBlanks;
		}

		// Returns whether this is an empty space (equivalent to '[0,0]')
		public boolean isEmpty() {
			return this.maxBlanks > 0;
		}

		// Returns whether this is a single space (i.e. 'a', '_', or '[1,1]')
		public boolean isSingular() {
			return this.minBlanks == 1 && this.maxBlanks == 1;
		}

		// Returns whether this blank can represent all characters
		public boolean isAllCharacters() {
			return this.possibleCharacters.equals(allCharacterSet());
		}

		// For testing purposes
		@Override
		public String toString() {
			StringBuilder charStr = new StringBuilder();
			if (this.negated) {
				charStr.append('^');
				Set<Character> allChars = allCharacterSet();
				for (char c : this.possibleCharacters) {
					allChars.remove(c);
				}
				for (char c : allChars) {
					charStr.append(c);
				}
			} else {
				for (char c : this.possibleCharacters) {
					charStr.append(c);
				}
			}
			return "[" + this.minBlanks + "," + this.maxBlanks + charStr.toString() + "]";
		}
	}
}
