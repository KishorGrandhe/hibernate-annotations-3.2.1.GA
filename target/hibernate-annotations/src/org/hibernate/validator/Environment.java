//$Id: $
package org.hibernate.validator;

/**
 * Hibernate Validator Event properties
 * The properties are retrieved from Hibernate
 * (hibernate.properties, hibernate.cfg.xml, persistence.xml or Configuration API)
 *
 * @author Emmanuel Bernard
 */
public class Environment {
	/**
	 * Message interpolator class used. The same instance is shared across all ClassValidators
	 */
	public static final String MESSAGE_INTERPOLATOR_CLASS = "hibernate.validator.message_interpolator_class";
}
