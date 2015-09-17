//$Id: Future.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Check that a Date, a Calendar, or a string representation apply in the future
 *
 * @author Gavin King
 */
@Documented
@ValidatorClass(FutureValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Future {
	String message() default "{validator.future}";
}
