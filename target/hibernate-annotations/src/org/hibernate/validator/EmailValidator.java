//$Id: EmailValidator.java 10021 2006-06-15 19:07:03Z epbernard $
package org.hibernate.validator;

import java.io.Serializable;
import java.util.regex.Matcher;

/**
 * Check that a given string is a well-formed email address
 *
 * @author Emmanuel Bernard
 */
public class EmailValidator implements Validator<Email>, Serializable {
	private static String ATOM = "[^\\x00-\\x1F^\\(^\\)^\\<^\\>^\\@^\\,^\\(;^\\:^\\\\^\\\"^\\.^\\[^\\]^\\s]";
	private static String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*";
	private static String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";

	private java.util.regex.Pattern pattern;

	public boolean isValid(Object value) {
		if ( value == null ) return true;
		if ( !( value instanceof String ) ) return false;
		String string = (String) value;
		Matcher m = pattern.matcher( string );
		return m.matches();
	}

	public void initialize(Email parameters) {
		pattern = java.util.regex.Pattern.compile(
				"^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*$",
				java.util.regex.Pattern.CASE_INSENSITIVE
		);
		pattern = java.util.regex.Pattern.compile(
				"^" + ATOM + "+(\\." + ATOM + "+)*@"
						 + DOMAIN
						 + "|"
						 + IP_DOMAIN
						 + ")$",
				java.util.regex.Pattern.CASE_INSENSITIVE
		);
	}
}
