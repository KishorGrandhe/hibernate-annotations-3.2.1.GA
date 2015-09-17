//$Id: IncorrectEntity.java 7098 2005-06-09 18:34:52Z epbernard $
package org.hibernate.test.annotations;

import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class IncorrectEntity {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
