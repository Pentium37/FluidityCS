package com.fluidity.program.utilities;

import java.math.BigDecimal;

/**
 * @author Yalesan Thayalan {@literal <yalesan2006@outlook.com>}
 */
public class ExtraMath {
	public static double roundToTwoDecimalPlaces(double number) {
		// Using Math.round() to round the number to two decimal places
		return Math.round(number * 100.0) / 100.0;
	}

	public static double roundToFourDecimalPlaces(double number) {
		// Using Math.round() to round the number to four decimal places
		return Math.round(number * 10000.0) / 10000.0;
	}
}
