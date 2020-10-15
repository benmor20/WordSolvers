package utils;

import java.util.Scanner;

public class GetUserInput {
	
	public static int getInt(String phrase) {
		int i = 0;
		Scanner scan = new Scanner(System.in);
		
		while(true) {
			System.out.print(phrase);
			if(scan.hasNextInt()) {
				i = scan.nextInt();
				break;
			}
			else {
				System.out.println("Please enter an integer.");
				scan.next();
			}
		}
		
		return i;
	}
	public static int getInt(String phrase, int min, int max) {
		int i = 0;
		Scanner scan = new Scanner(System.in);
		
		if(max < min) {
			int hold = max;
			max = min;
			min = hold;
		}
		
		while(true) {
			System.out.print(phrase);
			if(scan.hasNextInt()) {
				i = scan.nextInt();
				if(i > max) System.out.println("Too high. Enter a number less than " + max + ".");
				else if(i < min) System.out.println("Too low. Enter a number greater than " + min + ".");
				else break;
			}
			else {
				System.out.println("Please enter an integer.");
				scan.next();
			}
		}
		
		return i;
	}
	
	public static String getString(String phrase) {
		Scanner sc = new Scanner(System.in);
		System.out.print(phrase);
		return sc.next();
	}
	public static String getString(String phrase, String[] ans, boolean caseSensitive) {
		Scanner sc = new Scanner(System.in);
		while(true) {
			System.out.print(phrase);
			String str = sc.next();
			
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
		Scanner scan = new Scanner(System.in);
		while(true) {
			System.out.print(phrase);
			yn = scan.nextLine().toLowerCase();
			if(yn.equals("y") || yn.equals("yes") || yn.equals("true")) return true;
			else if(yn.equals("n") || yn.equals("no") || yn.equals("false")) return false;
			else System.out.println("Enter yes or no.");
		}
	}
}
