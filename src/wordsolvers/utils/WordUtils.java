package wordsolvers.utils;

import wordsolvers.structs.DictionaryNode;
import wordsolvers.structs.UnknownWord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class WordUtils {
	public static List<String> dictionary;
	private static DictionaryNode dictTree;
	private static Map<String, Long> frequencies;
	private static Map<String, Integer> rankings;
	private static List<String> wordsRanked;

	private static void readDict(String dictPath, String frequencyPath) {
		try {
			String line;
			dictionary = new ArrayList<>();
			frequencies = new HashMap<>();
			rankings = new HashMap<>();
			wordsRanked = new ArrayList<>();
			int rank = 0;

			BufferedReader freqReader = new BufferedReader(new FileReader(frequencyPath));
			while ((line = freqReader.readLine()) != null) {
				String[] info = line.toLowerCase().split("\t");
				frequencies.put(info[0], Long.parseLong(info[1]));
				rankings.put(info[0], rank);
				wordsRanked.add(info[0]);
				rank++;
			}
			freqReader.close();

			BufferedReader dictReader = new BufferedReader(new FileReader(dictPath));
			while ((line = dictReader.readLine()) != null) {
				dictionary.add(line.toLowerCase());
			}
			dictReader.close();

			dictTree = createDictionaryTree(dictionary);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				System.out.println(o);
			}
		}
	}
	public static void readDict() {
		String path = System.getProperty("user.dir") + "\\src\\data\\";
		readDict(path + "words.txt", path + "frequencies.txt");
	}

	public static boolean isWord(String word) {
		if (dictTree == null) readDict();
		return dictTree.hasChild(cleanWord(word));
	}
	public static boolean isPhrase(String phrase) {
		String[] words = phrase.split(" ");
		for (String word : words) {
			if (!isWord(word)) return false;
		}
		return true;
	}

	public static double scorePhrase(String phrase) {
		return scorePhrase(phrase.split(" "));
	}
	public static double scorePhrase(String[] phrase) {
		double score = 0;
		for (String word : phrase) {
			score += getRanking(word);
		}
		return score / phrase.length;
	}

	public static Map<String, Double> mapToScores(Collection<String> phrases) {
		Map<String, Double> map = new HashMap<>();
		for (String phrase : phrases) {
			if (map.containsKey(phrase)) continue;
			map.put(phrase, scorePhrase(phrase));
		}
		return map;
	}

	public static long getFrequency(String word) {
		if (frequencies == null) readDict();
		Long freq = frequencies.get(cleanWord(word));
		return freq == null ? 0 : freq;
	}
	public static double getRanking(String word) {
		if (rankings == null) readDict();
		Integer rank = rankings.get(cleanWord(word));
		if (rank == null && dictionary.contains(word)) rank = rankings.size();
		return rank == null ? WORST_RANKING : rank;
	}
	public static final int WORST_RANKING = 100000;

	public static String cleanWord(String word) {
		StringBuilder clean = new StringBuilder();
		for (char c : word.toLowerCase().toCharArray()) {
			if (c >= 'a' && c <= 'z') clean.append(c);
		}
		return clean.toString();
	}

	public static List<String> getDict() {
		if (dictionary == null) readDict();
		return new ArrayList<>(dictionary);
	}
	public static DictionaryNode getDictTree() {
		if (dictTree == null) readDict();
		return dictTree;
	}
	public static List<String> getOrderedWords() {
		if (wordsRanked == null) readDict();
		return new ArrayList<>(dictionary);
	}

	public static String getWordFromRanking(int ranking) {
		if (wordsRanked == null) readDict();
		return wordsRanked.get(ranking);
	}

	public static DictionaryNode createDictionaryTree(List<String> words) {
		DictionaryNode topNode = new DictionaryNode();
		for (String word : words) {
			DictionaryNode currentNode = topNode;
			word = cleanWord(word);
			for (int index = 0; index < word.length() - 1; index++) {
				char c = word.charAt(index);
				if (currentNode.hasChild(c)) currentNode = currentNode.getChild(c);
				else currentNode = currentNode.addChild(c, false);
			}
			char last = word.charAt(word.length() - 1);
			if (currentNode.hasChild(last)) currentNode.getChild(last).setIsWord(true);
			else currentNode.addChild(last, true);
		}
		return topNode;
	}

	public static DictionaryNode createTreeWithMostCommonWords(int numWords) {
		if (wordsRanked == null) readDict();
		return createDictionaryTree(wordsRanked.subList(0, numWords));
	}

	public static boolean isBetween(int value, int side1, int side2, boolean inclusive) {
		return inclusive ? (value >= side1 && value <= side2) || (value <= side1 && value >= side2)
				: (value > side1 && value < side2) || (value < side1 && value > side2);
	}

	// Returns a list containing each letter from a to z
	public static Set<Character> allCharacterSet() {
		return new HashSet<>() {{
			for (char c = 'a'; c <= 'z'; c++) {
				this.add(c);
			}
		}};
	}

	public static int addWithOverflow(int a, int b) {
		long sum = ((long) a) + ((long) b);
		if (sum > (long)Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int)sum;
	}

	public static <T> List<T> withoutIndex(List<T> list, int index) {
		List<T> ret = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			if (i == index) continue;
			T ele = list.get(i);
			ret.add(ele);
		}
		return ret;
	}

	public static DictionaryNode intersection(DictionaryNode node, List<UnknownWord> wordList) {
		DictionaryNode root = new DictionaryNode();
		boolean hasLetters = false;
		List<UnknownWord> listMinus1 = new ArrayList<>();
		for (UnknownWord word : wordList) {
			if (word.maxSpaces() > 0) {
				hasLetters = true;
				UnknownWord cut = word.cutFirstLetter();
				if (cut.maxSpaces() > 0) {
					listMinus1.add(cut);
				}
			}
		}
		if (!hasLetters) {
			return node;
		}

		for (UnknownWord word : wordList) {
			Set<Character> possPaths = word.getEffectiveSpace(0).possibleCharacters;
			for (char c : possPaths) {
				if (node.hasChild(c)) {
					if (!root.hasChild(c)) {
						root.addChild(c, intersection(node.getChild(c), listMinus1));
					}
					if (word.minSpaces() == 0) {
						root.getChild(c).setIsWord(true);
					}
				}
			}
		}
		return node;
	}
}
