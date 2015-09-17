//$Id: SerializabilityValidator.java 8593 2005-11-17 18:12:11Z epbernard $
package org.hibernate.validator.test;

import java.io.Serializable;

import org.hibernate.validator.Validator;

/**
 * Sample of a bean-level validator
 *
 * @author Emmanuel Bernard
 */
public class SerializabilityValidator implements Validator<Serializability>, Serializable {
	public boolean isValid(Object value) {
		return value instanceof Serializable;
	}

	public void initialize(Serializability parameters) {

	}
}
