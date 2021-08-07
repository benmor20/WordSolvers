package wordsolvers.utils;

import java.util.Collection;
import java.util.Scanner;

public class GetUserInput {
	private static final Scanner SCANNER = new Scanner(System.in);
	
	public static int getInt(String phrase) {
		int i = 0;

		while(true) {
			System.out.print(phrase);
			if(SCANNER.hasNextInt()) {
				i = SCANNER.nextInt();
				break;
			}
			else {
				System.out.println("Please enter an integer.");
				SCANNER.next();
			}
		}
		
		return i;
	}
	public static int getInt(String phrase, int min, int max) {
		int i = 0;

		if(max < min) {
			int hold = max;
			max = min;
			min = hold;
		}
		
		while(true) {
			System.out.print(phrase);
			if(SCANNER.hasNextInt()) {
				i = SCANNER.nextInt();
				if(i > max) System.out.println("Too high. Enter a number less than " + max + ".");
				else if(i < min) System.out.println("Too low. Enter a number greater than " + min + ".");
				else break;
			}
			else {
				System.out.println("Please enter an integer.");
				SCANNER.next();
			}
		}
		
		return i;
	}
	
	public static String getString(String phrase) {
		System.out.print(phrase);
		return SCANNER.nextLine();
	}
	public static String getString(String phrase, String[] ans, boolean caseSensitive) {
		while(true) {
			System.out.print(phrase);
			String str = SCANNER.nextLine();
			
			for (String poss : ans) {
				if(caseSensitive) {
					if(poss.equals(str)) return str.toLowerCase();
				}
				else {
					if(poss.equalsIgnoreCase(str)) return str;
				}
			}
			
			System.out.println("Please enter one of the options.");
		}
	}
	public static String getString(String phrase, Collection<String> ans, boolean caseSensitive) {
		while(true) {
			System.out.print(phrase);
			String str = SCANNER.nextLine();

			for (String poss : ans) {
				if(caseSensitive) {
					if(poss.equals(str)) return str.toLowerCase();
				}
				else {
					if(poss.equalsIgnoreCase(str)) return str;
				}
			}

			System.out.println("Please enter one of the options.");
		}
	}
	
	public static boolean getBoolean(String phrase) {
		String yn = "";
		while(true) {
			System.out.print(phrase);
			yn = SCANNER.nextLine().toLowerCase();
			if(yn.equals("y") || yn.equals("yes") || yn.equals("true")) return true;
			else if(yn.equals("n") || yn.equals("no") || yn.equals("false")) return false;
			else System.out.println("Enter yes or no.");
		}
	}
}
