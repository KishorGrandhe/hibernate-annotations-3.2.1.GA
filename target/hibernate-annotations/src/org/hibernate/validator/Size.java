//$Id: Size.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Size range for Arrays, Collections or Maps
 *
 * @author Gavin King
 */
@Documented
@ValidatorClass(SizeValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Size {
	int max() default Integer.MAX_VALUE;

	int min() default 0;

	String message() default "{validator.size}";
}
