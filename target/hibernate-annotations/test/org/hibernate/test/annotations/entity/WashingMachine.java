//$Id: WashingMachine.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class WashingMachine {
	@Id
	@GeneratedValue
	private Integer id;
	private transient boolean isActive;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}
}
