package wordsolvers.main;

import wordsolvers.utils.GetUserInput;
import wordsolvers.utils.WordUtils;

import java.util.*;

public class CipherSolver {
	public static void main(String[] args) {
		String cipher = GetUserInput.getString("Enter the cipher. ");

		if (args.length > 0 && args[0].equals("c")) {
			for (int offset = 0; offset < 26; offset++) {
				System.out.println(caesar(cipher, offset));
			}
			return;
		} else if (args.length > 0) {
			System.out.println("Unknown code: " + args[0]);
		}
		System.out.println("  CAESAR: " + caesar(cipher));
		System.out.println("  ATBASH: " + atbash(cipher));
		System.out.println("   A1Z26: " + a1z26(cipher));

		String vigenereKey = GetUserInput.getString("\nEnter key for Vigenere, or e to exit: ");
		if (!vigenereKey.equalsIgnoreCase("e") && vigenereKey.length() > 0) {
			System.out.println("VIGENERE: " + vigenere(cipher, vigenereKey));
		}
	}

	public static String reverse(String input) {
		return new StringBuilder(input).reverse().toString();
	}

	public static String caesar(String input, int offset) {
		StringBuilder res = new StringBuilder();
		for (char c : input.toCharArray()) {
			if (c >= 'a' && c <= 'z') {
				char shift = (char)(c + offset);
				res.append(shift > 'z' ? (char)(shift - 26) : shift);
			} else if (c >= 'A' && c <= 'Z') {
				char shift = (char)(c + offset);
				res.append(shift > 'Z' ? (char)(shift - 26) : shift);
			} else {
				res.append(c);
			}
		}
		return res.toString();
	}

	public static String caesar(String input) {
		String[] decodedMessages = new String[26];
		int maxCount = 0, maxCountOffset = -1;
		for (int offset = 0; offset < 26; offset++) {
			decodedMessages[offset] = caesar(input, offset);
			int numWords = 0;
			for (String word : decodedMessages[offset].split(" ")) {
				if (WordUtils.isWord(word)) numWords++;
			}
			if (numWords > maxCount) {
				maxCount = numWords;
				maxCountOffset = offset;
			}
		}
		return maxCountOffset == -1 ? "" : decodedMessages[maxCountOffset];
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
