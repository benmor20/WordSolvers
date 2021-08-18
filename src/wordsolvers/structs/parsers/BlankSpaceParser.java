package wordsolvers.structs.parsers;

import wordsolvers.structs.BlankSpace;
import wordsolvers.utils.WordUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlankSpaceParser extends Parser<BlankSpace> {
    public BlankSpaceParser() {
        super(BlankSpace.class);
    }

    @Override
    public String serialize(BlankSpace obj) {
        return obj.toString();
    }

    @Override
    public BlankSpace parse(String str) {
        // Empty string
        if (str == null || str.length() == 0) {
            return null;
        }

        String unknown = str.toLowerCase();

        // '_' or single letter
        if (unknown.length() == 1) {
            if (unknown.equals("_")) {
                return new BlankSpace(false);
            }
            char lett = unknown.charAt(0);
            if (lett >= 'a' && lett <= 'z') {
                return new BlankSpace(false, lett);
            }
            this.throwParseError(str, "Single-character BlankSpace must be a letter or underscore", 0);
        }

        // If length is > 1, must be bracket
        char bracket = unknown.charAt(0);
        if (bracket != '[' && bracket != '{' && bracket != '(') {
            this.throwParseError(str, "Multiple character BlankSpace must start with [, {, or (", 0);
        }
        char endBracket = unknown.charAt(str.length() - 1);
        if ((bracket == '[' && endBracket != ']') || (bracket == '{' && endBracket != '}')
                || (bracket == '(' && endBracket != ')')) {
            this.throwParseError(str, "Last character must match the starting bracket", str.length() - 1);
        }

        // Parse inside bracket
        unknown = unknown.substring(1, str.length() - 1);
        if (unknown.length() == 0) {
            this.throwParseError(str, "Missing characters inside bracket", 1);
        }
        int index = 0;
        boolean isFilter = bracket != '[';
        int min = 1, max = isFilter ? Integer.MAX_VALUE : 1; // Default bounds

        // If first char is a number, has lower bound
        char c = unknown.charAt(index);
        if (c >= '0' && c <= '9') { // Set minumum bounds
            min = 0;
            for (; c >= '0' && c <= '9'; index++, c = unknown.charAt(index)) {
                min *= 10;
                min += c - '0';
                if (index == unknown.length() - 1) {
                    this.throwParseError(str, "Missing comma", index + 1);
                }
            }
        }
        if (c == ',') { // Either has just min, just max, or both
            // No minimum bound (did not go into previous if statement)
            if (index == 0) {
                min = 0;
            }

            // Skip comma
            index++;
            if (index < unknown.length()) {
                c = unknown.charAt(index);

                if (c >= '0' && c <= '9') { // Set maximum bounds
                    max = 0;
                    for (; c >= '0' && c <= '9'; index++, c = unknown.charAt(index)) {
                        max *= 10;
                        max += c - '0';
                        if (index == unknown.length() - 1) {
                            index++;
                            break;
                        }
                    }
                } else { // No maximum
                    max = Integer.MAX_VALUE;
                }
            } else { // No maximum or character limits
                max = Integer.MAX_VALUE;
            }
        }

        if (min > max) this.throwParseError(str, "Lower bound (" + min + ") cannot be more than upper bound ("
                + max + ")", index + 1);

        if (index < unknown.length()) { // Character limits given
            boolean negated = false;
            if (c == '^') { // One space, find negation
                negated = true;
                index++;
            }

            if (index < unknown.length()) {
                BlankSpace ret = new BlankSpace(true, min, max, negated);
                c = unknown.charAt(index);
                if (c >= 'a' && c <= 'z') {
                    for (; c >= 'a' && c <= 'z'; index++, c = unknown.charAt(index)) {
                        ret.appendLetter(c);
                        if (index == unknown.length() - 1) {
                            return ret;
                        }
                    }
                }
                this.throwParseError(str, "Unexpected character: " + c, index + 1);
            } else { // ^ ends brackets
                return new BlankSpace(true, min, max, true);
            }
        } else { // Only bounds given
            return new BlankSpace(true, min, max, false, WordUtils.allCharacterSet());
        }

        // Unreachable - either throws error or returns before here
        return null;
    }
}
