package wordsolvers.structs.parsers;

import wordsolvers.structs.BlankSpace;
import wordsolvers.structs.UnknownWord;

import java.util.ArrayList;
import java.util.List;

public class UnknownWordParser extends Parser<UnknownWord> {
    private final BlankSpaceParser spaceParser;

    public UnknownWordParser() {
        super(UnknownWord.class);
        this.spaceParser = new BlankSpaceParser();
    }

    public String serialize(UnknownWord obj) {
        return obj.toString();
    }

    public UnknownWord parse(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }

        String unknown = str.toLowerCase();
        List<BlankSpace> spaces = new ArrayList<>();
        BlankSpace filter = null;
        boolean hardFilter = false;
        for (int index = 0; index < str.length(); index++) {
            char c = unknown.charAt(index);
            if (c == '_' || (c >= 'a' && c <= 'z')) {
                spaces.add(this.spaceParser.parse(c + ""));
            } else if (c == '[' || c == '{' || c == '(') {
                int endIndex = getClosingBracketIndex(unknown, index);
                BlankSpace space = this.spaceParser.parse(unknown.substring(index, endIndex + 1));
                index = endIndex;
                if (c == '[') {
                    spaces.add(space);
                } else {
                    filter = space;
                    hardFilter = c == '{';
                    if (index != str.length() - 1) {
                        this.throwParseError(str, "Filter must be at the end of the word", endIndex);
                    }
                }
            } else {
                this.throwParseError(str, "Unknown character " + c, index);
            }
        }
        return new UnknownWord(spaces, filter, hardFilter);
    }

    public static int getClosingBracketIndex(String unknown, int start) {
        // Determine bracket type
        char c = unknown.charAt(start);
        char endBracket = switch (c) {
            case '[' -> ']';
            case '{' -> '}';
            case '(' -> ')';
            default -> throw new IllegalArgumentException(c + " is not a bracket");
        };

        // Get substring of inside of bracket
        int startIndex = start + 1, endIndex = startIndex;
        for (c = unknown.charAt(endIndex); c != endBracket; endIndex++, c = unknown.charAt(endIndex)) {
            if (endIndex == unknown.length() - 1) {
                throw new IllegalArgumentException("No closing brackets for bracket at index " + start + ".");
            }
        }
        return endIndex;
    }
}
