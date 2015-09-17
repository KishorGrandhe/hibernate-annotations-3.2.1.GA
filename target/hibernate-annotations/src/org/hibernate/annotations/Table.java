//$Id: Table.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Complementary information to a table either primary or secondary
 *
 * @author Emmanuel Bernard
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface Table {
	/**
	 * name of the targeted table
	 */
	String appliesTo();

	/**
	 * Indexes
	 */
	Index[] indexes() default {};

}
