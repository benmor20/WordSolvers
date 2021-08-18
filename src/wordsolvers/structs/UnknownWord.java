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

    public Collection<String> possibleWords(DictionaryNode wordTree) {
        Collection<String> ret = new LinkedHashSet<>(); // Preserve order (roughly alphabetical) but prevent repeats

        // Ensure bounds are kept
        int[] bounds = this.getLengthBounds();
        int minLen = bounds[0], maxLen = bounds[1];
        if (this.hasFilter() && (minLen > this.filter.maxBlanks || maxLen < this.filter.minBlanks)) {
            return ret;
        }

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
            ret.addAll(this.cutFirstSpace().possibleWords(wordTree));
        }
        for (char c : firstSpace.possibleCharacters) {
            if (!wordTree.hasChild(c)) {
                continue;
            }
            ret.addAll(cutFirst.possibleWords(wordTree.getChild(c)));
        }
        return ret;
    }

    private UnknownWord cutFirstLetter() {
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

    private UnknownWord cutFirstSpace() {
        return new UnknownWord(this.spaces.subList(1, this.spaces.size()), this.filter, this.hardFilter);
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
}
