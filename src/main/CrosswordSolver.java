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
		for (int index = 0; index < unknown.length(); index++) {
			char c = unknown.charAt(index);
			if (c == '_') {
				spaces.add(new BlankSpace(false));
			} else if (c >= 'a' && c <= 'z') {
				spaces.add(new BlankSpace(false, c));
			} else if (c == '[') {
				// Get substring of inside of bracket
				int startIndex = index + 1, endIndex = startIndex;
				for (c = unknown.charAt(endIndex); c != ']'; endIndex++, c = unknown.charAt(endIndex)) {
					if (endIndex == unknown.length() - 1) {
						throw new IllegalArgumentException("No closing brackets for bracket at index " + index + ".");
					}
				}
				String inner = unknown.substring(startIndex, endIndex);

				// If nothing inside brackets, throw exception
				if (inner.length() == 0) throw new IllegalArgumentException("Empty brackets (index " + index + ").");

				int innerIndex = 0;
				c = inner.charAt(innerIndex);
				int min = 1, max = 1; // Default bounds
				if (c >= '0' && c <= '9') { // Set minumum bounds
					min = 0;
					for (; c >= '0' && c <= '9'; innerIndex++, c = inner.charAt(innerIndex)) {
						min *= 10;
						min += c - '0';
						if (innerIndex == inner.length() - 1) {
							throw new IllegalArgumentException("Missing comma at index " + (index + innerIndex + 1) + ".");
						}
					}
					if (c != ',') throw new IllegalArgumentException("Missing comma at index " + (index + innerIndex + 1) + ".");
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

				if (innerIndex < inner.length()) { // Letter limits given
					boolean negated = false;
					if (c == '^') { // One space, find negation
						negated = true;
						innerIndex++;
					}

					if (innerIndex < inner.length()) {
						BlankSpace blank = new BlankSpace(true, min, max, negated);
						c = inner.charAt(innerIndex);
						if (c >= 'a' && c <= 'z') {
							for (; c >= 'a' && c <= 'z'; innerIndex++, c = inner.charAt(innerIndex)) {
								blank.appendLetter(c);
								if (innerIndex == inner.length() - 1) {
									break;
								}
							}
							spaces.add(blank);
						} else {
							throw new IllegalArgumentException("Unexpected character: " + c + " (index " + (index + innerIndex + 1) + ").");
						}
					} else { // ^ ends brackets
						spaces.add(new BlankSpace(true, min, max, true));
					}
				} else { // Only bounds given
					spaces.add(new BlankSpace(true, min, max, false, allCharacterSet()));
				}
				index += inner.length() + 1; // Skip the bracket expression
			} else if (c == '{') {
				// Apply filter
				Collection<Character> filter = getFilter(unknown, index);
				for (BlankSpace space : spaces) {
					space.applyFilter(true, filter);
				}

				// Can only make it this far if at end of input
				break;
			} else if (c == '(') {
				// Apply filter
				Collection<Character> filter = getFilter(unknown, index);
				for (BlankSpace space : spaces) {
					space.applyFilter(false, filter);
				}

				// Can only make it this far if at end of input
				break;
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
		Collection<String> words = possibleWords(spaces, WordUtils.getDictTree());

		// Print out possibilities
		if (words.size() == 0) {
			System.out.println("Sorry, no words found.");
		} else {
			for (String word : words) {
				System.out.println(word);
			}
		}
	}

	private static Collection<Character> getFilter(String unknown, int start) {
		// Get substring of inside of bracket
		int startIndex = start + 1, endIndex = startIndex;
		char endChar = unknown.charAt(start) == '{' ? '}' : ')';
		for (char c = unknown.charAt(endIndex); c != endChar; endIndex++, c = unknown.charAt(endIndex)) {
			if (endIndex == unknown.length() - 1) {
				throw new IllegalArgumentException("No closing brackets for bracket at index " + start + ".");
			}
		}

		// Ensure the filter ends the input
		if (endIndex < unknown.length() - 1) {
			throw new IllegalArgumentException("Symbols found after filter: " + unknown.substring(endIndex + 1));
		}

		String inner = unknown.substring(startIndex, endIndex);
		if (inner.length() == 0) throw new IllegalArgumentException("Empty filter (index: " + start + ")");

		// Check if filter is negated. If it is, cut '^' from inner
		boolean negated = false;
		if (inner.charAt(0) == '^') {
			negated = true;
			inner = inner.substring(1);
		}

		// Determine filter
		Set<Character> filter = new HashSet<>();
		for (int innerIndex = 0; innerIndex < inner.length(); innerIndex++) {
			char f = inner.charAt(innerIndex);
			if (f < 'a' || f > 'z') {
				throw new IllegalArgumentException("Unexpected character: " + f + " (index: " + (start + innerIndex + 1) + ")");
			}
			filter.add(f);
		}

		// Negate if necessary
		if (negated) {
			Set<Character> temp = allCharacterSet();
			temp.removeAll(filter);
			filter = temp;
		}

		return filter;
	}

	private static Collection<String> possibleWords(List<BlankSpace> letters, DictionaryNode currentNode) {
		Collection<String> ret = new LinkedHashSet<>(); // Preserve order (roughly alphabetical) but prevent repeats

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
			ret.addAll(possibleWords(skipFirst, currentNode));
		}
		// If the max is also 0, return, otherwise, continue
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
				ret.addAll(possibleWords(newLetters, currentNode.getChild(c)));
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
