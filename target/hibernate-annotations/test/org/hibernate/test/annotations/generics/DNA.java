//$Id: Address.java 8967 2006-01-03 12:27:34Z epbernard $
package org.hibernate.test.annotations.generics;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Paolo Perrotta
 */
@Entity
public class DNA {

	private Integer id;

	@Id
	@GeneratedValue
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
