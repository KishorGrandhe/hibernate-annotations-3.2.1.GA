//$Id: $
package org.hibernate.validator.test.validator;

import org.hibernate.validator.NotEmpty;

/**
 * @author Emmanuel Bernard
 */
public class Car {
	@NotEmpty
	public String name;
	@NotEmpty
	public String[] insurances;
}
