//$Id: Robot.java 6760 2005-05-12 13:34:04Z epbernard $
package org.hibernate.test.annotations.tableperclass;

import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Robot extends Machine {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
