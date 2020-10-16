package utils;

public class Utils {
	public static boolean isBetween(int value, int side1, int side2, boolean inclusive) {
		return inclusive ? (side1 <= side2 && value >= side1 && value <= side2) || (side1 >= side2 && value <= side1 && value >= side2)
				: (side1 < side2 && value > side1 && value < side2) || (side1 > side2 && value < side1 && value > side2);
	}
}
