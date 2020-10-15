package main;

import utils.WordUtils;

import java.util.*;

public class WordJumble {
	private static final int JUMBLE_LEN = 4;
	private static final int MIN_WORD_LEN = 6;
	private static final int NUM_ACCEPTABLE_WORDS = 4000;

	public static void main(String[] args) {
		Random rand = new Random();
		List<String> currentWords = new ArrayList<>();
		for (int wordNum = 0; wordNum < JUMBLE_LEN; wordNum++) {
			int wordRanking = rand.nextInt(NUM_ACCEPTABLE_WORDS);
			String word = WordUtils.getWordFromRanking(wordRanking);
			while (currentWords.contains(word) || word.length() < MIN_WORD_LEN) {
				wordRanking = rand.nextInt(NUM_ACCEPTABLE_WORDS);
				word = WordUtils.getWordFromRanking(wordRanking);
			}
			currentWords.add(word);

			char[] array = word.toCharArray();
			List<Character> shuffled = new ArrayList<>();
			for (char c : array) {
				shuffled.add(c);
			}
			Collections.shuffle(shuffled, rand);

			for (char c: shuffled) {
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
