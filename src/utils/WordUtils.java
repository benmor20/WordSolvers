package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordUtils {
	public static List<String> dictionary;
	private static DictionaryNode dictTree;
	private static Map<String, Long> frequencies;
	private static Map<String, Integer> rankings;

	private static void readDict(String dictPath, String frequencyPath) {
		try {
			String line;
			dictionary = new ArrayList<>();
			frequencies = new HashMap<>();
			rankings = new HashMap<>();
			int rank = 1;

			BufferedReader freqReader = new BufferedReader(new FileReader(frequencyPath));
			while ((line = freqReader.readLine()) != null) {
				String[] info = line.toLowerCase().split("\t");
				frequencies.put(info[0], Long.parseLong(info[1]));
				rankings.put(info[0], rank);
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
		return dictionary;
	}
	public static DictionaryNode getDictTree() {
		if (dictTree == null) readDict();
		return dictTree;
	}

	public static String getWordFromRanking(int ranking) {
		if (rankings == null) readDict();
		for (Map.Entry<String, Integer> rankingEntry : rankings.entrySet()) {
			if (rankingEntry.getValue() == ranking) return rankingEntry.getKey();
		}
		return null;
	}

	public static DictionaryNode createDictionaryTree(List<String> words) {
		DictionaryNode topNode = new DictionaryNode();
		for (String word : words) {
			DictionaryNode currentNode = topNode;
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
}
