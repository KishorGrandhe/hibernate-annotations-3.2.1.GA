//$Id: Name.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.validator.test.inheritance;

import org.hibernate.validator.NotNull;

/**
 * @author Emmanuel Bernard
 */
public interface Name {
	@NotNull
	String getName();

	void setName(String name);
}
