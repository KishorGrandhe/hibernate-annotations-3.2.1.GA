//$Id: Cat.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.manytomany;

import java.util.Set;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * @author Emmanuel Bernard
 */
@Entity
@Table(name = "tbl_cat")
public class Cat {
	private CatPk id;
	private int age;
	private Set<Woman> humanContacts;

	@ManyToMany
	public Set<Woman> getHumanContacts() {
		return humanContacts;
	}

	public void setHumanContacts(Set<Woman> humanContacts) {
		this.humanContacts = humanContacts;
	}

	@EmbeddedId()
	public CatPk getId() {
		return id;
	}

	public void setId(CatPk id) {
		this.id = id;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( !( o instanceof Cat ) ) return false;

		final Cat cat = (Cat) o;

		if ( !id.equals( cat.id ) ) return false;

		return true;
	}

	public int hashCode() {
		return id.hashCode();
	}
}
