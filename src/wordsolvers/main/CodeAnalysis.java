package wordsolvers.main;

import wordsolvers.utils.GetUserInput;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class CodeAnalysis {
    // Letters by frequency, counted by full text (words can repeat so common words dominate)
    private static final char[] LETTERS_BY_TEXT = {'e', 't', 'a', 'o', 'i', 'n', 's', 'h', 'r', 'd', 'l', 'c', 'u', 'm', 'w', 'f', 'g', 'y', 'p', 'b', 'v', 'k', 'x', 'j', 'q', 'z'};

    // Letters by frequency, where each word is counted once
    private static final char[] LETTERS_BY_WORD = {'e', 's', 'i', 'a', 'r', 'n', 't', 'o', 'l', 'c', 'd', 'u', 'g', 'p', 'm', 'h', 'b', 'y', 'f', 'v', 'k', 'w', 'z', 'x', 'j', 'q'};

    public static void main(String[] args) {
        boolean byText = true;
        boolean wordsToChars = true;

        String code = GetUserInput.getString("Enter the code. ").toLowerCase();
        if (wordsToChars) {
            Map<String, Character> key = getWordMapping(code, byText);
            System.out.println(decode(code.split(" "), key));
            System.out.println();
        }
        else {
            Map<Character, Character> key = getMapping(code, byText);
            for (char c = 'a'; c <= 'z'; c++) {
                System.out.println(c + ": " + key.get(c));
            }
            System.out.println(decode(code, key));
            System.out.println();
        }
    }

    public static String decode(String[] code, Map<String, Character> key) {
        StringBuilder res = new StringBuilder();
        for (String word : code) {
            res.append(key.get(word));
        }
        return res.toString();
    }

    public static String decode(String code, Map<Character, Character> key) {
        StringBuilder res = new StringBuilder();
        for (char c : code.toCharArray()) {
            if (key.containsKey(c)) {
                res.append(key.get(c));
            } else {
                res.append(c);
            }
        }
        return res.toString();
    }

    public static Map<String, Character> getWordMapping(String code, boolean byText) {
        return getWordMapping(code.split(" "), byText);
    }

    public static Map<String, Character> getWordMapping(String[] code, boolean byText) {
        char[] freq = byText ? LETTERS_BY_TEXT : LETTERS_BY_WORD;
        Map<String, Integer> counts = new HashMap<>();
        for (String word : code) {
            counts.put(word, counts.getOrDefault(word, 0) + 1);
        }

        List<String> words = new ArrayList<>(counts.keySet());
        words.sort((a, b) -> counts.get(b) - counts.get(a));
        Map<String, Character> freqMap = new HashMap<>();
        for (int i = 0; i < Math.min(words.size(), 26); i++) {
            freqMap.put(words.get(i), freq[i]);
        }
        return freqMap;
    }

    public static Map<Character, Character> getMapping(String code, boolean byText) {
        char[] freq = byText ? LETTERS_BY_TEXT : LETTERS_BY_WORD;

        Map<Character, Integer> counts = new HashMap<>();
        for (char c = 'a'; c <= 'z'; c++) {
            counts.put(c, 0);
        }

        for (char c : code.toCharArray()) {
            if (c < 'a' || c > 'z') {
                continue;
            }
            counts.put(c, counts.get(c) + 1);
        }

        List<Character> chars = new ArrayList<>(counts.keySet());
        chars.sort((a, b) -> counts.get(b) - counts.get(a));
        Map<Character, Character> freqMapping = new HashMap<>();
        for (int i = 0; i < 26; i++) {
            freqMapping.put(chars.get(i), freq[i]);
        }

        return freqMapping;
    }
}
