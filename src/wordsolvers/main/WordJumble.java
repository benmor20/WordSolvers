package wordsolvers.main;

import wordsolvers.structs.BlankSpace;
import wordsolvers.structs.DictionaryNode;
import wordsolvers.structs.UnknownWord;
import wordsolvers.utils.WordUtils;

import java.util.*;

public class WordJumble {
	private static final int JUMBLE_LEN = 4;
	private static final int MIN_WORD_LEN = 6;
	private static final int NUM_ACCEPTABLE_WORDS = 4000;

	public static void main(String[] args) {
		Random rand = new Random();
		UnknownWord pattern = new UnknownWord(new ArrayList<>() {{
			this.add(new BlankSpace(true, 6, Integer.MAX_VALUE, true));
		}});
		DictionaryNode tree = WordUtils.createTreeWithMostCommonWords(NUM_ACCEPTABLE_WORDS);
		List<String> allWords = new ArrayList<>(pattern.possibleWords(tree));

		Collection<String> currentWords = new LinkedHashSet<>();
		while (currentWords.size() < JUMBLE_LEN) {
			currentWords.add(allWords.get(rand.nextInt(allWords.size())).toUpperCase());
		}

		for (String word : currentWords) {
			List<Character> shuffledLetters = new ArrayList<>();
			for (char c : word.toCharArray()) {
				shuffledLetters.add(c);
			}
			Collections.shuffle(shuffledLetters);
			for (char c : shuffledLetters) {
				System.out.print(c);
			}
			System.out.println();
		}

		Scanner scan = new Scanner(System.in);
		System.out.println("\nHit enter to reveal the words");
		scan.nextLine();

		for (String word : currentWords) {
			System.out.println(word);
		}
	}
}
