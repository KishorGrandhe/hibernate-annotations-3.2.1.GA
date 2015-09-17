//$Id: FilterDefs.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Array of filter definitions
 *
 * @author Matthew Inger
 * @author Emmanuel Bernard
 */
@Target({PACKAGE, TYPE})
@Retention(RUNTIME)
public @interface FilterDefs {
	FilterDef[] value();
}
