package wordsolvers.main;

import wordsolvers.structs.DictionaryNode;
import wordsolvers.utils.WordUtils;

import java.util.*;

public class DualLetterSolver {
    private static final String GRID = "JO EX D OL IA R E GE VE LE\n" +
            "SE SA W EN ER FI OC CA AL LT\n" +
            "PA NT WA DR ER ST TZ W T UR\n" +
            "AL A PE RN A Z OO KD RA LD\n" +
            "MO HN AN LI F D UM SE A T\n" +
            "CA IC RS NO NA VI LL ER U AM\n" +
            "M EA Z G GE AN U SB U LL\n" +
            "A LL HI RN AN BU CK EC ET Y\n" +
            "I RI FO AR IS H T AG HI LF\n" +
            "K LI I O IA VE NK I IT RG";

    public static void main(String[] args) {
        String[] rows = GRID.split("\n");
        char[][][] grid = new char[rows.length][rows.length][];
        for (int rowi = 0; rowi < rows.length; rowi++) {
            String[] words = rows[rowi].split(" ");
            for (int wordi = 0; wordi < words.length; wordi++) {
                grid[rowi][wordi] = words[wordi].toCharArray();
            }
        }

        printGrid(grid);

        char[][][][] grids = splitGrid(grid);
        for (char[][][] miniGrid : grids) {
            printGrid(miniGrid);
            List<Set<String>>[] possibleWords = parseGrid(miniGrid, WordUtils.getDictTree());
            List<Set<String>> rowWords = possibleWords[0];
            List<Set<String>> colWords = possibleWords[1];

            System.out.println("ROWS:");
            for (int i = 0; i < rowWords.size(); i++) {
                System.out.print(i + 1 + ": ");
                for (String word : rowWords.get(i)) {
                    System.out.print(word + ", ");
                }
                System.out.println();
            }
            System.out.println("\nCOLUMNS:");
            for (int i = 0; i < colWords.size(); i++) {
                System.out.print(i + 1 + ": ");
                for (String word : colWords.get(i)) {
                    System.out.print(word + ", ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    public static List<Set<String>>[] parseGrid(char[][][] grid, DictionaryNode root) {
        List<Set<String>> rows = new ArrayList<>();
        List<Set<String>> cols = new ArrayList<>();
        for (char[][] row : grid) {
            rows.add(parseLine(row, root));
        }
        for (int coli = 0; coli < grid[0].length; coli++) {
            char[][] col = new char[grid[0].length][];
            for (int rowi = 0; rowi < grid.length; rowi++) {
                col[rowi] = grid[rowi][coli];
            }
            cols.add(parseLine(col, root));
        }
        return new List[] {rows, cols};
    }

    public static Set<String> parseLine(char[][] line, DictionaryNode node) {
        Set<String> res = new HashSet<>();
        if (line.length == 0) {
            if (node.isWord()) {
                res.add(node.toString());
            }
            return res;
        }
        for (char c : line[0]) {
            if (node.hasChild(c)) {
                res.addAll(parseLine(Arrays.copyOfRange(line, 1, line.length), node.getChild(c)));
            }
        }
        return res;
    }

    public static char[][][][] splitGrid(char[][][] grid) {
        int rows = grid.length, cols = grid[0].length;
        if (rows != cols || rows % 2 == 1) {
            throw new IllegalArgumentException("Invalid grid size: " + rows + "x" + cols);
        }
        int halfLen = rows / 2;
        char[][][][] split = new char[4][halfLen][halfLen][];

        for (int row = 0; row < halfLen; row++) {
            split[0][row] = Arrays.copyOfRange(grid[row], 0, halfLen);
            split[1][row] = Arrays.copyOfRange(grid[row], halfLen, rows);
        }
        for (int row = halfLen; row < rows; row++) {
            split[2][row - halfLen] = Arrays.copyOfRange(grid[row], 0, halfLen);
            split[3][row - halfLen] = Arrays.copyOfRange(grid[row], halfLen, rows);
        }
        return split;
    }

    public static void printGrid(char[][][] grid) {
        for (char[][] row : grid) {
            for (char[] cs : row) {
                if (cs == null) {
                    System.out.print("null ");
                    continue;
                }
                for (char c : cs) {
                    System.out.print(c);
                }
                System.out.print(" ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
