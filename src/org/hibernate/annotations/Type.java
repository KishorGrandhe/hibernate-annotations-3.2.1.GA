//$Id: Type.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * hibernate type
 *
 * @author Emmanuel Bernard
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
public @interface Type {
	String type();

	Parameter[] parameters() default {};
}