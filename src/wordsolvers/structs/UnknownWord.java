package wordsolvers.structs;

import wordsolvers.utils.WordUtils;

import java.util.*;

public class UnknownWord {
    private final List<BlankSpace> spaces;
    private final BlankSpace filter;
    private final boolean hardFilter;

    public UnknownWord(List<BlankSpace> spaces, BlankSpace filter, boolean hardFilter) {
        this.spaces = new ArrayList<>();
        for (BlankSpace space : spaces) {
            this.spaces.add(space.clone());
        }
        this.filter = filter == null? null : filter.clone();
        if (this.filter != null && !this.filter.isBracketed()) {
            throw new IllegalArgumentException("Filter needs to be bracketed");
        }
        this.hardFilter = hardFilter;
    }
    public UnknownWord(List<BlankSpace> spaces) {
        this(spaces, null, false);
    }

    public boolean hasFilter() {
        return this.filter != null;
    }
    public BlankSpace getFilter() {
        return this.filter;
    }
    public boolean isHardFilter() {
        return this.hardFilter;
    }

    // Returns whether this word can only represent one thing
    public boolean isDeterminant() {
        for (BlankSpace space : this.spaces) {
            if (!space.isDeterminant()) {
                return false;
            }
        }
        return true;
    }

    public List<BlankSpace> getSpaces() {
        return this.spaces;
    }
    public BlankSpace getSpace(int index) {
        return this.spaces.get(index);
    }

    public List<BlankSpace> getEffectiveSpaces() {
        if (!this.hasFilter()) {
            return this.getSpaces();
        }
        List<BlankSpace> effectiveSpaces = new ArrayList<>();
        for (BlankSpace space : this.spaces) {
            effectiveSpaces.add(space.withFilter(this.filter.possibleCharacters, this.hardFilter));
        }
        return effectiveSpaces;
    }
    public BlankSpace getEffectiveSpace(int index) {
        if (!this.hasFilter()) {
            return this.getSpace(index);
        }
        return this.spaces.get(index).withFilter(this.filter.possibleCharacters, this.hardFilter);
    }

    public UnknownWord cutSpace(int index) {
        BlankSpace space = this.spaces.get(index);
        BlankSpace filter = this.hasFilter() ? this.filter.cloneMinusSpace(space.minBlanks, space.maxBlanks) : null;
        return new UnknownWord(WordUtils.withoutIndex(this.spaces, index), filter, this.hardFilter);
    }

    // Returns a 2 element array containing the minimum and maximum word length
    private int[] getLengthBounds() {
        int min = 0, max = 0;
        for (BlankSpace space : this.spaces) {
            min += space.minBlanks;
            max = WordUtils.addWithOverflow(max, space.maxBlanks);
        }
        return new int[] { min, max };
    }
    public int minSpaces() {
        return this.getLengthBounds()[0];
    }
    public int maxSpaces() {
        return this.getLengthBounds()[1];
    }

    private boolean isPossible() {
        int[] bounds = this.getLengthBounds();
        int minLen = bounds[0], maxLen = bounds[1];
        return !this.hasFilter() || (minLen <= this.filter.maxBlanks && maxLen >= this.filter.minBlanks);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (BlankSpace space : this.spaces) {
            str.append(space.toString());
        }
        if (!this.hasFilter()) {
            return str.toString();
        }
        String filter = this.filter.toString();
        if (this.hardFilter) {
            filter = filter.replace('[', '{').replace(']', '}');
        } else {
            filter = filter.replace('[', '(').replace(']', ')');
        }
        return str + filter;
    }

    public Collection<String> possibleAnagrams(DictionaryNode wordTree) {
        return this.possibleAnagrams(wordTree, 1);
    }
    public Collection<String> possibleAnagrams(DictionaryNode wordTree, int maxWords) {
        Collection<String> ret = new LinkedHashSet<>();

        if (!this.isPossible()) return ret;
        if (this.spaces.size() == 0) {
            if (wordTree.isWord()) ret.add(wordTree.toString());
            return ret;
        }
        if (wordTree.isWord() && maxWords > 1) {
            //System.out.println("Max words: " + maxWords);
            String word = wordTree.toString();
            Collection<String> newWords = this.possibleAnagrams(wordTree.getRoot(), maxWords - 1);
            for (String newWord : newWords) {
                List<String> wordsToSort = new ArrayList<>();
                wordsToSort.add(word);
                Collections.addAll(wordsToSort, newWord.split(" "));
                Collections.sort(wordsToSort);
                StringBuilder sortedPhrase = new StringBuilder();
                for (String sortedWord : wordsToSort) {
                    sortedPhrase.append(sortedWord);
                    sortedPhrase.append(" ");
                }
                ret.add(sortedPhrase.substring(0, sortedPhrase.length() - 1));
            }
        }

        for (int index = 0; index < this.spaces.size(); index++) {
            BlankSpace space = this.getEffectiveSpace(index);
            UnknownWord withoutSpace = this.cutSpace(index);
            Collection<DictionaryNode> possibleNodes = wordTree.getChildrenFrom(space);
            for (DictionaryNode child : possibleNodes) {
                ret.addAll(withoutSpace.possibleAnagrams(child, maxWords));
            }
        }
        return ret;
    }

    public Collection<String> possibleWords(DictionaryNode wordTree) {
        Collection<String> ret = new LinkedHashSet<>(); // Preserve order (roughly alphabetical) but prevent repeats

        // Ensure bounds are kept
        if (!this.isPossible()) return ret;

        // If no more letters, return list containing current word, or empty list if currentNode is not a word
        UnknownWord cutFirst = this.cutFirstLetter();
        if (cutFirst == null) { // null iff no more letters
            if (wordTree.isWord()) {
                ret.add(wordTree.toString());
            }
            return ret;
        }

        // If there is at least one letter left, get the possible characters (w/ filter) and recurse
        BlankSpace firstSpace = this.getEffectiveSpace(0);
        if (firstSpace.minBlanks == 0) {
            ret.addAll(this.cutSpace(0).possibleWords(wordTree));
        }
        for (char c : firstSpace.possibleCharacters) {
            if (c == ' ') {
                if (wordTree.isWord()) {
                    String word = wordTree.toString();
                    Collection<String> next = cutFirst.possibleWords(wordTree.getRoot());
                    for (String phrase : next) {
                        ret.add(word + " " + phrase);
                    }
                }
                continue;
            }
            if (!wordTree.hasChild(c)) {
                continue;
            }
            ret.addAll(cutFirst.possibleWords(wordTree.getChild(c)));
        }
        return ret;
    }

    public UnknownWord cutFirstLetter() {
        int maxLetters = this.getLengthBounds()[1];
        if (maxLetters == 0) {
            return null;
        } else if (maxLetters == 1) {
            return new UnknownWord(new ArrayList<>(), this.hasFilter() ? this.filter.cloneOneLess() : null,
                    this.hardFilter);
        }
        BlankSpace firstBlank = this.spaces.get(0);
        List<BlankSpace> skipFirst = new ArrayList<>();
        for (int index = 1; index < this.spaces.size(); index++) {
            skipFirst.add(this.spaces.get(index));
        }
        if (firstBlank.maxBlanks == 0) {
            return new UnknownWord(skipFirst, this.filter, this.hardFilter).cutFirstLetter();
        } else {
            if (firstBlank.maxBlanks > 1) skipFirst.add(0, firstBlank.cloneOneLess());
            return new UnknownWord(skipFirst, this.hasFilter() ? this.filter.cloneOneLess() : null, this.hardFilter);
        }
    }
}
