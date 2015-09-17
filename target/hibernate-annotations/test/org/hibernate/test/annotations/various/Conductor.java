//$Id: Conductor.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.various;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Index;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Conductor {
	@Id
	@GeneratedValue
	private Integer id;

	@Column(name = "cond_name")
	@Index(name = "cond_name")
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
