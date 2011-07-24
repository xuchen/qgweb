package net.sourceforge.fuzzyhashmap;


public class StringMetrics {

	///////////////////////////////////////////////////////////////////////////////
	// Soundex																vvv
	///////////////////////////////////////////////////////////////////////////////
	/**
	 * Calculates the soundex code for the given string - work good for English language
	 */
	public static String soundexHash(String s) {
		char[] x = s.toUpperCase().toCharArray();
		char firstLetter = x[0];

	        // convert letters to numeric code
	        for (int i = 0; i < x.length; i++) {
	            switch (x[i]) {
	                case 'B':
	                case 'F':
	                case 'P':
	                case 'V': { x[i] = '1'; break; }

	                case 'C':
	                case 'G':
	                case 'J':
	                case 'K':
	                case 'Q':
	                case 'S':
	                case 'X':
	                case 'Z': { x[i] = '2'; break; }

	                case 'D':
	                case 'T': { x[i] = '3'; break; }

	                case 'L': { x[i] = '4'; break; }

	                case 'M':
	                case 'N': { x[i] = '5'; break; }

	                case 'R': { x[i] = '6'; break; }

	                default:  { x[i] = '0'; break; }
	            }
	        }

	        // remove duplicates
	        String output = "" + firstLetter;
	        for (int i = 1; i < x.length; i++)
	            if (x[i] != x[i-1] && x[i] != '0')
	                output += x[i];

	        // pad with 0's or truncate
	        output = output + "0000";
	        return output.substring(0, 4);
	  }
	///////////////////////////////////////////////////////////////////////////////
	// Soundex																^^^
	///////////////////////////////////////////////////////////////////////////////
	// Levenshtein Distance													vvv
	///////////////////////////////////////////////////////////////////////////////
	/**
	 * calculates the Levenshtein distance between str1 and str1
	 */
	public static int computeLevenshteinDistance(CharSequence str1, CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 0; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));

		return distance[str1.length()][str2.length()];
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}
	///////////////////////////////////////////////////////////////////////////////
	// Levenshtein Distance													^^^
	///////////////////////////////////////////////////////////////////////////////


}
