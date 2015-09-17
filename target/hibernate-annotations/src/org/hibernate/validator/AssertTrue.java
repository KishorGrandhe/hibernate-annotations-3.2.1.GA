//$Id: AssertTrue.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * The annotated element has to be true
 *
 * @author Gavin King
 */
@Documented
@ValidatorClass(AssertTrueValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface AssertTrue {
	String message() default "{validator.assertTrue}";
}
