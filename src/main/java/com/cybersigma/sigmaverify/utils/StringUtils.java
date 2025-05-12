package com.cybersigma.sigmaverify.utils;




public class StringUtils {
	
	public static String toTitleCase(String input) {
		// TODO: change the string into title case
	    StringBuilder titleCase = new StringBuilder(input.length());
	    boolean nextTitleCase = true;

	    for (char c : input.toCharArray()) {
	        if (Character.isSpaceChar(c)) {
	            nextTitleCase = true;
	        } else if (nextTitleCase) {
	            c = Character.toTitleCase(c);
	            nextTitleCase = false;
	        }

	        titleCase.append(c);
	    }

	    return titleCase.toString();
	}

	/**
	 * Get Full Name
	 * @param firstName - first name should not be null
	 * @param middleName - middle name may be null
	 * @param lastName - last name may be null
	 * @return full name with Title Case
	 */
	public static String toFullName(String firstName, String middleName, String lastName){
		return toTitleCase ((firstName + (middleName!= null || !middleName.isEmpty()? (" "+middleName) : "") + (lastName!= null ? (" "+lastName):"")).trim().replaceAll(" {2}", " "));
	}
	public static String symbolList ="!@#$%^&*()_+{}[]";
}
