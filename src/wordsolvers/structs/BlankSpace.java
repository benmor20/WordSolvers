package wordsolvers.structs;

import wordsolvers.utils.WordUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BlankSpace implements Cloneable {
    // Possible characters for this blank space
    public final Set<Character> possibleCharacters;

    // When this is negated, keeps track of removed characters
    public final Set<Character> removedCharacters;

    // How many blanks this space can represent
    public final int minBlanks, maxBlanks;

    // Whether, when appending characters, to subtract from possibleCharacters (true) or to add (false)
    // Equivalently, whether there is a ^ operator
    private final boolean negated;

    // Whether this space was given as brackets ('a' vs '[a]', or '_' vs '[^]' or '[abc...xyz]'), which matters for filters)
    private final boolean bracketed;

    public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks, boolean negated, Collection<Character> cs) {
        this(bracketed, minBlanks, maxBlanks, negated);
        this.appendAll(cs);
    }
    public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks, boolean negated, char c) {
        this(bracketed, minBlanks, maxBlanks, negated);
        this.appendLetter(c);
    }
    public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks, boolean negated) {
        this.possibleCharacters = negated ? WordUtils.allCharacterSet() : new HashSet<>();
        this.removedCharacters = new HashSet<>();
        this.minBlanks = minBlanks;
        this.maxBlanks = maxBlanks;
        this.negated = negated;
        this.bracketed = bracketed;
    }
    public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks, Collection<Character> cs) {
        this(bracketed, minBlanks, maxBlanks);
        this.appendAll(cs);
    }
    public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks, char c) {
        this(bracketed, minBlanks, maxBlanks);
        this.appendLetter(c);
    }
    public BlankSpace(boolean bracketed, int minBlanks, int maxBlanks) {
        this(bracketed, minBlanks, maxBlanks, false);
    }
    public BlankSpace(boolean bracketed, boolean negated, Collection<Character> cs) {
        this(bracketed, negated);
        this.appendAll(cs);
    }
    public BlankSpace(boolean bracketed, boolean negated, char c) {
        this(bracketed, negated);
        this.appendLetter(c);
    }
    public BlankSpace(boolean bracketed, boolean negated) {
        this(bracketed, 1, 1, negated);
    }
    public BlankSpace(boolean bracketed, Collection<Character> cs) {
        this(bracketed, false, cs);
    }
    public BlankSpace(boolean bracketed, char c) {
        this(bracketed, false);
        this.appendLetter(c);
    }
    public BlankSpace(boolean bracketed) {
        this(bracketed, false);
        // If no letters specified and not negated, all letters allowed
        this.addAll(WordUtils.allCharacterSet());
    }

    // Returns a BlankSpace with one less blank
    public BlankSpace subtractBlank() {
        if (this.minBlanks == 0 && this.maxBlanks == 0) throw new IllegalStateException("Cannot subtract BlankSpace with no spaces");
        BlankSpace ret = new BlankSpace(this.bracketed, Math.max(this.minBlanks - 1, 0), this.maxBlanks - 1, this.negated);
        copyCharacters(this, ret);
        return ret;
    }

    // Adds or subtracts the given character from the range of possibilities, depending on negation
    public void appendLetter(char c) {
        if (this.negated) {
            this.removeLetter(c);
        } else {
            this.addLetter(c);
        }
    }
    // Repeats appendLetter(c) for each element of cs
    public void appendAll(Collection<Character> cs) {
        if (this.negated) {
            this.removeAll(cs);
        } else {
            this.addAll(cs);
        }
    }

    // Add c to the list of possible characters
    public void addLetter(char c) {
        this.removedCharacters.remove(c);
        this.possibleCharacters.add(c);
    }
    // Repeats addLetter(c) for each element of cs
    public void addAll(Collection<Character> cs) {
        this.removedCharacters.removeAll(cs);
        this.possibleCharacters.addAll(cs);
    }

    // Remove c from the list of possible characters
    public void removeLetter(char c) {
        this.possibleCharacters.remove(c);
        this.removedCharacters.add(c);
    }
    // Repeats removeLetter(c) for each element of cs
    public void removeAll(Collection<Character> cs) {
        this.possibleCharacters.removeAll(cs);
        this.removedCharacters.addAll(cs);
    }

    // Applies a filter over all the letters
    public void applyFilter(Collection<Character> filter, boolean hardFilter) {
        applyFilterTo(this, filter, hardFilter);
    }

    // Returns a BlankSpace with the given filter applied
    public BlankSpace withFilter(Collection<Character> filter, boolean hardFilter) {
        BlankSpace ret = this.clone();
        applyFilterTo(ret, filter, hardFilter);
        return ret;
    }

    // Applies the given filter to the given BlankSpace
    private static void applyFilterTo(BlankSpace space, Collection<Character> filter, boolean hardFilter) {
        // See README for the filter rules
        if ((hardFilter && (space.bracketed || space.possibleCharacters.size() > 1))
                || (!hardFilter && !space.bracketed && space.possibleCharacters.size() > 1)) {
            Set<Character> toRemove = WordUtils.allCharacterSet();
            toRemove.removeAll(filter);
            space.removeAll(toRemove);
        }
    }

    // Returns whether this BlankSpace can represent a variable number of blanks (i.e. '[2,5]')
    public boolean hasVariableBlanks() {
        return this.minBlanks != this.maxBlanks;
    }

    // Returns whether this is an empty space (equivalent to '[0,0]')
    public boolean isEmpty() {
        return this.maxBlanks == 0;
    }

    // Returns whether this is a single space (i.e. 'a', '_', or '[1,1]')
    public boolean isSingular() {
        return this.minBlanks == 1 && this.maxBlanks == 1;
    }

    // Returns whether this blank can represent all characters
    public boolean isAllCharacters() {
        return this.possibleCharacters.equals(WordUtils.allCharacterSet());
    }

    // Returns whether this blank is bracketed
    public boolean isBracketed() {
        return this.bracketed;
    }

    // Returns whether this blank can represent exactly one thing
    public boolean isDeterminant() {
        return this.possibleCharacters.size() == 1 && this.minBlanks == this.maxBlanks;
    }

    @Override
    public String toString() {
        if (!this.bracketed && !this.negated && this.possibleCharacters.size() == 1) {
            return this.possibleCharacters.toArray()[0].toString();
        }
        if (!this.bracketed && !this.negated && this.possibleCharacters.size() == 26) {
            return "_";
        }
        StringBuilder charStr = new StringBuilder();
        if (this.negated) {
            charStr.append('^');
            Set<Character> allChars = WordUtils.allCharacterSet();
            for (char c : this.possibleCharacters) {
                allChars.remove(c);
            }
            for (char c : allChars) {
                charStr.append(c);
            }
        } else if (!this.isAllCharacters()) {
            for (char c : this.possibleCharacters) {
                charStr.append(c);
            }
        }
        if (this.minBlanks == 1 && this.maxBlanks == 1) {
            return "[" + charStr + "]";
        }
        String minStr = this.minBlanks == 0 ? "" : this.minBlanks + "";
        String maxStr = this.maxBlanks == Integer.MAX_VALUE ? "" : this.maxBlanks + "";
        return "[" + minStr + "," + maxStr + charStr + "]";
    }

    @Override
    public BlankSpace clone() {
        BlankSpace clone = new BlankSpace(this.bracketed, this.minBlanks, this.maxBlanks, this.negated);
        copyCharacters(this, clone);
        return clone;
    }

    public BlankSpace cloneOneLess() {
        return this.cloneMinusSpace(1, 1);
    }
    public BlankSpace cloneMinusSpace(int min, int max) {
        if (this.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a new BlankSpace with less blanks than none");
        }
        int newMin = Math.max(0, this.minBlanks - max),
                newMax = this.maxBlanks == Integer.MAX_VALUE ? Integer.MAX_VALUE : this.maxBlanks - min;
        BlankSpace clone = new BlankSpace(this.bracketed, newMin, newMax, this.negated);
        copyCharacters(this, clone);
        return clone;
    }

    private static void copyCharacters(BlankSpace from, BlankSpace to) {
        to.possibleCharacters.clear();
        to.removedCharacters.clear();
        if (from.negated) {
            if (to.negated) {
                to.possibleCharacters.addAll(WordUtils.allCharacterSet());
                to.possibleCharacters.removeAll(from.removedCharacters);
                to.removedCharacters.addAll(from.removedCharacters);
            } else {
                to.possibleCharacters.addAll(from.removedCharacters);
            }
        } else {
            if (to.negated) {
                to.possibleCharacters.addAll(WordUtils.allCharacterSet());
                to.possibleCharacters.removeAll(from.possibleCharacters);
                to.removedCharacters.addAll(from.possibleCharacters);
            } else {
                to.possibleCharacters.addAll(from.possibleCharacters);
            }
        }
    }
}
