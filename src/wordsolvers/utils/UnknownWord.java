package wordsolvers.utils;

import java.util.ArrayList;
import java.util.List;

public class UnknownWord {
    private final List<BlankSpace> spaces;
    private final BlankSpace filter;
    private final boolean hardFilter;

    public UnknownWord(List<BlankSpace> spaces, BlankSpace filter, boolean hardFilter) {
        this.spaces = new ArrayList<>();
        for (BlankSpace space : spaces) {
            this.spaces.add(space.clone());
        }
        this.filter = filter;
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
        return str.toString() + filter;
    }
}
