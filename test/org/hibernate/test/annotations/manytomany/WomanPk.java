//$Id: WomanPk.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.manytomany;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 * @author Emmanuel Bernard
 */
@Embeddable
public class WomanPk implements Serializable {


	private String firstName;
	private String lastName;

	public int hashCode() {
		//this implem sucks
		return getFirstName().hashCode() + getLastName().hashCode();
	}

	public boolean equals(Object obj) {
		//firstName and lastName are expected to be set in this implem
		if ( obj != null && obj instanceof WomanPk ) {
			WomanPk other = (WomanPk) obj;
			return getFirstName().equals( other.getFirstName() )
					&& getLastName().equals( other.getLastName() );
		}
		else {
			return false;
		}
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}