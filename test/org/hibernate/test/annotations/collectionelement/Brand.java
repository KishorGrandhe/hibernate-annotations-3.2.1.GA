//$Id: Brand.java 10354 2006-08-28 04:33:01Z epbernard $
package org.hibernate.test.annotations.collectionelement;

import javax.persistence.Embeddable;

/**
 * @author Emmanuel Bernard
 */
@Embeddable
public class Brand {
	private String name;
	private String surname;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( o == null || getClass() != o.getClass() ) return false;

		final Brand brand = (Brand) o;

		if ( !name.equals( brand.name ) ) return false;

		return true;
	}

	public int hashCode() {
		return name.hashCode();
	}
}
