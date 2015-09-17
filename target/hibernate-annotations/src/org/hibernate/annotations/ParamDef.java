//$Id: ParamDef.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.annotations;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * A parameter definition
 *
 * @author Emmanuel Bernard
 */
@Target({})
@Retention(RUNTIME)
public @interface ParamDef {
	String name();

	String type();
}
