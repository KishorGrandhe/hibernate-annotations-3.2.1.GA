//$Id: NotNull.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * not null constraint
 *
 * @author Gavin King
 */
@Documented
@ValidatorClass(NotNullValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface NotNull {
	String message() default "{validator.notNull}";
}
