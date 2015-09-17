//$Id: Country.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.entity;

import java.io.Serializable;

/**
 * Serializable object to be serialized in DB as is
 *
 * @author Emmanuel Bernard
 */
public class Country implements Serializable {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
