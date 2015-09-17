//$Id: Manager.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.embedded;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Manager {
	private Integer id;
	private String name;
	private InternetProvider employer;

	@ManyToOne
	public InternetProvider getEmployer() {
		return employer;
	}

	public void setEmployer(InternetProvider employer) {
		this.employer = employer;
	}

	@Id
	@GeneratedValue
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