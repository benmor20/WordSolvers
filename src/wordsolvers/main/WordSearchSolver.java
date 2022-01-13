package wordsolvers.main;

import wordsolvers.structs.BlankSpace;
import wordsolvers.structs.DictionaryNode;
import wordsolvers.structs.UnknownWord;
import wordsolvers.structs.parsers.UnknownWordParser;
import wordsolvers.utils.GetUserInput;
import wordsolvers.utils.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordSearchSolver {
	private static final String WORD_SEARCH =
			("I\tS\tY\tO\tT\tS\tI\tM\tE\tH\tC\tL\tR\tD\tF\n" +
					"N\tG\tY\tL\tD\tE\tR\tU\tS\tS\tA\tC\tI\tI\tR\n" +
					"T\tN\tB\tY\tI\tE\tT\tS\tE\tI\tO\tM\tL\tS\tH\n" +
					"O\tI\tA\tE\tL\tD\tR\tW\tT\tR\tM\tA\tL\tP\tI\n" +
					"N\tS\tR\tW\tR\tE\tE\tN\tN\tI\tM\tK\tE\tE\tN\n" +
					"I\tI\tO\tI\tL\tR\tE\tE\tN\tE\tC\tS\tS\tN\tT\n" +
					"N\tR\tM\tL\tE\tR\tR\tE\tN\tO\tN\tC\tO\tS\tE\n" +
					"G\tA\tE\tD\tE\tE\tN\tT\tL\tH\tA\tI\tD\tE\tR\n" +
					"D\tS\tT\tF\tD\tT\tE\tD\tE\tL\tG\tN\tI\tS\tL\n" +
					"N\tM\tE\tE\tS\tS\tA\tA\tA\tG\tE\tI\tN\tT\tA\n" +
					"H\tR\tR\tE\tS\tP\tE\tT\tA\tR\tO\tR\tP\tE\tC\n" +
					"P\tS\tP\tO\tT\tS\tE\tD\tI\tS\tF\tF\tO\tS\tE").replaceAll("\t", "");
	private static final int MIN_WORD_LENGTH = 3;

	public static void main(String[] args) {
		System.out.println(WORD_SEARCH);
		DictionaryNode dict = getWordList();

		// Reformat WORD_SEARCH into a char[][]
		String[] wsByRow = WORD_SEARCH.split("\n");
		char[][] wordSearch = new char[wsByRow.length][];
		for (int row = 0; row < wsByRow.length; row++) {
			wordSearch[row] = wsByRow[row].toLowerCase().toCharArray();
		}
		List<FoundWordInfo> foundWords = findWords(wordSearch, dict);

		// Display words
		int numWords = foundWords.size();
		if (numWords == 0) {
			System.out.println("Sorry, no words found.");
			return;
		}
		System.out.println("Search complete! Found " + numWords + " word" + (numWords == 1 ? "" : "s") + ":");
		List<String> words = new ArrayList<>(); // list of options to input
		Map<String, List<FoundWordInfo>> wordsToInfo = new HashMap<>();
		for (FoundWordInfo wordInfo : foundWords) {
			System.out.println(wordInfo.word);
			words.add(wordInfo.word);
			if (wordsToInfo.get(wordInfo.word) == null) {
				wordsToInfo.put(wordInfo.word, new ArrayList<>() {{
					this.add(wordInfo);
				}});
			} else {
				wordsToInfo.get(wordInfo.word).add(wordInfo);
			}
		}
		words.add("e"); // can also input "e" for exit

		// Have user select word to show location of
		while (true) {
			String wordToDisplay = GetUserInput.getString("Type the word you want to find the location of, or type 'e' to exit. ",
					words, false);
			if (wordToDisplay.equalsIgnoreCase("e")) break;
			for (FoundWordInfo wordInfo : wordsToInfo.get(wordToDisplay)) {
				int len = wordInfo.word.length();
				int[] scales = directionToScales(wordInfo.direction);
				int maxRow = wordInfo.startRow + scales[0] * (len - 1),
						maxCol = wordInfo.startCol + scales[1] * (len - 1); // might not be max, but the isBetween function will handle that case

				// Print board, capitalizing the word
				for (int row = 0; row < wordSearch.length; row++) {
					for (int col = 0; col < wordSearch[row].length; col++) {
						// Determine capitalization
						boolean capitalize;
						// Reverse calculate the number of steps out this would be. MAX_VALUE as flag for orthogonal, which would result in division by 0
						int rowOffset = scales[0] == 0 ? Integer.MAX_VALUE : (row - wordInfo.startRow) / scales[0],
								colOffset = scales[1] == 0 ? Integer.MAX_VALUE : (col - wordInfo.startCol) / scales[1];
						if (rowOffset == Integer.MAX_VALUE) { // Horizontal
							capitalize = row == wordInfo.startRow && WordUtils.isBetween(col, wordInfo.startCol, maxCol, true);
						} else if (colOffset == Integer.MAX_VALUE) { // Vertical
							capitalize = col == wordInfo.startCol && WordUtils.isBetween(row, wordInfo.startRow, maxRow, true);
						} else { // Diagonal
							capitalize = rowOffset == colOffset && rowOffset >= 0 && rowOffset < len;
						}

						// Print
						// TODO still hard to see letters in word search - is bold an option?
						if (capitalize) {
							System.out.print("\033[0;1m" + (wordSearch[row][col] + " ").toUpperCase() + "\033[0;0m");
						} else {
							System.out.print(wordSearch[row][col] + " ");
						}
					}
					System.out.println();
				}
				System.out.println();
			}
		}
	}

	// Goes through the word search looking for words
	private static List<FoundWordInfo> findWords(char[][] wordSearch, DictionaryNode dict) {
		// Search for words
		boolean hasList = false;
		List<FoundWordInfo> foundWords = new ArrayList<>();
		for (int row = 0; row < wordSearch.length; row++) {
			for (int col = 0; col < wordSearch[row].length; col++) {
				char curChar = wordSearch[row][col];
				// If no word starts with the current letter, skip
				if (!dict.hasChild(curChar)) continue;

				// Create node array, one node for each way the word can go (orthogonal + diagonal)
				DictionaryNode[] words = new DictionaryNode[8];
				for (int initTree = 0; initTree < words.length; initTree++) {
					words[initTree] = dict.getChild(curChar);
				}

				// Propogate out, look for words
				boolean done = false;
				for (int step = 1; !done; step++) {
					done = true;
					for (int direction = 0; direction < words.length; direction++) {
						if (words[direction] == null) continue;
						done = false; // Still have valid words, can go another step out

						// Find working row/col
						int[] scales = directionToScales(direction);
						int rowCheck = row + step * scales[0],
								colCheck = col + step * scales[1];

						// If out of bounds, set current direction's node to null and continue
						if (rowCheck < 0 || rowCheck >= wordSearch.length) {
							words[direction] = null;
							continue;
						}
						else if (colCheck < 0 || colCheck >= wordSearch[rowCheck].length) {
							words[direction] = null;
							continue;
						}

						// Advance step, check if still on tree, or if found word
						words[direction] = words[direction].getChild(wordSearch[rowCheck][colCheck]);
						if (words[direction] == null) continue;
						if ((hasList || words[direction].currentDepth() >= MIN_WORD_LENGTH) && words[direction].isWord()) {
							foundWords.add(new FoundWordInfo(words[direction].toString(), row, col, direction));
						}
					}
				}
			}
		}

		return foundWords;
	}

	// Creates a dictionary tree based on user's word list
	public static DictionaryNode getWordList() {
		boolean hasList = GetUserInput.getBoolean("Is there a list of words provided? ");

		// Find word list, or compile dictionary as word list
		DictionaryNode dict = null;
		List<UnknownWord> wordList = new ArrayList<>();
		if (hasList) {
			UnknownWordParser parser = new UnknownWordParser();
			String word = GetUserInput.getString("Enter the first word on the list. ");
			while (!word.equalsIgnoreCase("d")) {
				word = word.replace(" ", "");
				wordList.add(parser.parse(word));
				word = GetUserInput.getString("Enter the next word on the list, or 'd' for done. ");
			}
			System.out.println();

			// If all are determinant, make tree. Else, use dictionary
			boolean customTree = true;
			for (UnknownWord unknown : wordList) {
				if (!unknown.isDeterminant()) {
					customTree = false;
				}
			}
			if (customTree) {
				List<String> detList = new ArrayList<>();
				for (UnknownWord unknown : wordList) {
					detList.add(parser.serialize(unknown));
				}
				dict = WordUtils.createDictionaryTree(detList);
			}
		}

		if (dict == null) { // If no word list provided, or list is not full determined, use full dictionary
			dict = WordUtils.getDictTree();
		}
		if (wordList.size() == 0) { // If no word list provided, possibilities are any word
			List<BlankSpace> space = new ArrayList<>() {{
				this.add(new BlankSpace(true, MIN_WORD_LENGTH, Integer.MAX_VALUE, true));
			}};
			wordList.add(new UnknownWord(space));
		}

		return WordUtils.intersection(dict, wordList);
	}

	private static class FoundWordInfo {
		public final String word;
		public final int startRow, startCol, direction;

		public FoundWordInfo(String word, int startRow, int startCol, int direction) {
			this.word = word;
			this.startRow = startRow;
			this.startCol = startCol;
			this.direction = direction;
		}
	}

	// Translate direction (0->7) to x-y direction (i.e. {1 -1} means down right)
	private static int[] directionToScales(int direction) {
		int[] scales = new int[2];
		scales[0] = (direction + 1) / 3;
		scales[1] = (direction + 1) % 3;
		if (scales[0] == 2) scales[0] = -1;
		if (scales[1] == 2) scales[1] = -1;
		return scales;
	}
}
