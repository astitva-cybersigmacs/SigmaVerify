package com.cybersigma.sigmaverify.utils;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Validations {
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);

	public String validateName(String name) {
		String mgs = "";
		if (name == null) {
			mgs = "Please enter email ";
			return mgs;
		} else if (name.length() < 3 || name.length() > 20) {
			mgs = "Please enter minimum 3 and maximum 20 character";
		} else if (Character.isDigit(name.charAt(0))) {
			mgs = "Name should not be start with digit";
		}
		return mgs;
	}

	public String validateEmail(String emailStr) {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
		String mgs = "";
		if (emailStr == null) {
			mgs = "Email should not be empty";
		} else if (!(matcher.matches())) {
			mgs = "email is not valid";
		}
		return mgs;
	}

	public String validatePhoneNo(String phoneNo) {
		// (0/91): number starts with (0/91)
		// [7-9]: starting of the number may contain a digit between 0 to 9
		// [0-9]: then contains digits 0 to 9
		Pattern ptrn = Pattern.compile("(0/91)?[7-9][0-9]{9}");
		// the matcher() method creates a matcher that will match the given input
		// against this pattern
		Matcher match = ptrn.matcher(phoneNo);
		String mgs = "";
		if (phoneNo == null) {
			mgs = "Please enter phoneNo ";
		} else if (phoneNo.length() != 10) {
			mgs = "Phone Number should be 10 digits";
		} else if (!(match.find() && match.group().equals(phoneNo))) {
			mgs = "Not a valid Phone Number";
		}
		return mgs;
	}

}
