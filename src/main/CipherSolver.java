package main;

import utils.WordUtils;

import java.util.*;

public class CipherSolver {
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter the cipher: ");
		String cipher = scan.nextLine();

		WordUtils.readDict();
		System.out.println("  CAESAR: " + caesar(cipher));
		System.out.println("  ATBASH: " + atbash(cipher));
		System.out.println("   A1Z26: " + a1z26(cipher));
		System.out.println("     ALL: " + caesar(atbash(a1z26(cipher))));
		System.out.println("     R/C: " + reverse(caesar(cipher), true));
		//System.out.println("VIGENERE: " + vigenereByWord(cipher));

		System.out.print("\nEnter key for Vigenere: ");
		String vigenereKey = scan.nextLine().split(" ")[0];
		if (vigenereKey.length() > 0) {
			System.out.println("VIGENERE: " + vigenere(cipher, vigenereKey));
		}
	}

	public static String reverse(String input, boolean reverseSpaces) {
		return new StringBuilder(input).reverse().toString();
	}

	public static String caesar(String input) {
		String[] cipherWords = input.split(" ");

		int[] numWords = new int[26];
		String[] decodedMessages = new String[26];
		int maxCount = 0, maxCountOffset = 0;
		for (int offset = 0; offset < 26; offset++) {
			StringBuilder decodedWords = new StringBuilder();
			for (int wordIndex = 0; wordIndex < cipherWords.length; wordIndex++) {
				String codedWord = cipherWords[wordIndex];
				StringBuilder decodedWord = new StringBuilder();
				for (char c : codedWord.toCharArray()) {
					char shiftedChar;
					if (c >= 'a' && c <= 'z') {
						shiftedChar = (char)(c + offset);
						if (shiftedChar > 'z') shiftedChar -= 26;
					} else if (c >= 'A' && c <= 'Z') {
						shiftedChar = (char)(c + offset);
						if (shiftedChar > 'Z') shiftedChar -= 26;
					} else {
						shiftedChar = c;
					}
					decodedWord.append(shiftedChar);
				}
				String word = decodedWord.toString();
				if (WordUtils.isWord(word)) numWords[offset]++;
				decodedWords.append(word);
				decodedWords.append(" ");
			}
			decodedMessages[offset] = decodedWords.toString();
			if (numWords[offset] > maxCount) {
				maxCount = numWords[offset];
				maxCountOffset = offset;
			}
		}
		return maxCountOffset == 0 ? "" : decodedMessages[maxCountOffset];
	}

	public static String atbash(String input) {
		char[] cipher = input.toCharArray();
		StringBuilder decoded = new StringBuilder();
		for (char c : cipher) {
			if (c >= 'a' && c <= 'z') {
				decoded.append((char)('z' - c + 'a'));
			} else if (c >= 'A' && c <= 'Z') {
				decoded.append((char)('Z' - c + 'A'));
			} else {
				decoded.append(c);
			}
		}
		return decoded.toString();
	}

	public static String a1z26(String input) {
		char[] cipher = input.toCharArray();
		char[] delimPoss = {'.', '-', ';', ','};
		Map<Character, Integer> delimCount = new HashMap<>();
		char delim = delimPoss[0];
		for (char c : cipher) {
			for (char poss : delimPoss) {
				if (c == poss) {
					if (delimCount.containsKey(c)) delimCount.put(c, delimCount.get(c) + 1);
					else delimCount.put(c, 1);
					if (!delimCount.containsKey(delim) || delimCount.get(c) > delimCount.get(delim)) delim = c;
				}
			}
		}

		StringBuilder decoded = new StringBuilder();
		int number = 0;
		for (char c : cipher) {
			if (c >= '0' && c <= '9') {
				number *= 10;
				number += c - '0';
			} else {
				if (number != 0) {
					decoded.append((char) (number + 'a' - 1));
					number = 0;
				}
				if (c != delim) {
					decoded.append(c);
				}
			}
		}
		if (number > 0) decoded.append((char)(number + 'a' - 1));
		return decoded.toString();
	}

	public static String vigenere(String cipher, String key) {
		int keyLen = key.length();
		StringBuilder decoded = new StringBuilder();
		int offset = 0;
		String lowerKey = key.toLowerCase();
		for (int index = 0; index < cipher.length(); index++) {
			char c = cipher.charAt(index),
					keyChar = lowerKey.charAt((index - offset) % keyLen);
			if (c >= 'a' && c <= 'z') {
				c -= keyChar - 'a';
				if (c < 'a') c += 26;
			} else if (c >= 'A' && c <= 'Z') {
				c -= keyChar - 'a';
				if (c < 'A') c += 26;
			} else {
				offset++;
			}
			decoded.append(c);
		}
		return decoded.toString();
	}

	public static String vigenereByWord(String cipher) {
		String bestDecode = "", bestKey = "";
		double bestScore = Integer.MAX_VALUE;
		int diff = 7;
		for (String key : WordUtils.getDict()) {
			if (diff % key.length() != 0) continue;
			String decoded = vigenere(cipher, key);
			String[] words = decoded.split(" ");
			if (WordUtils.getRanking(words[words.length - 1]) == WordUtils.WORST_RANKING) continue;
			double score = WordUtils.scorePhrase(words);
			if (score < bestScore) {
				bestScore = score;
				bestDecode = decoded;
				bestKey = key;
			}
		}
		return bestScore == 0 ? "" : bestDecode + " KEY: " + bestKey;
	}

	private static final double TOLERANCE = 0.75;
	private static final int MAX_LETTERS = 15;
	public static String vigenereConstructedKey(String cipher) {
		for (int numChars = 1; numChars <= MAX_LETTERS; numChars++) {
			char[] key = new char[numChars];
			for (int fillKey = 0; fillKey < numChars; fillKey++) {
				key[fillKey] = 'a';
			}
			while (true) {
				String decoded = vigenere(cipher, new String(key));
				String[] words = decoded.split(" ");

				double numWords = 0;
				for (String word : words) {
					if (WordUtils.isWord(word)) {
						numWords++;
					}
				}
				if (numWords / words.length >= TOLERANCE) return decoded + " KEY: " + new String(key);

				boolean exit = false;
				for (int index = 0; index < numChars; index++) {
					key[index]++;
					if (key[index] > 'z') {
						key[index] = 'a';
						if (index == numChars - 1) exit = true;
					}
					else break;
				}
				if (exit) break;
			}
		}
		return "";
	}
}
