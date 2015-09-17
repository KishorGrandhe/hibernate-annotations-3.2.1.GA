//$Id: Contact.java 8974 2006-01-04 00:43:49Z epbernard $
package org.hibernate.validator.test;

import javax.persistence.Embeddable;

import org.hibernate.validator.NotNull;

/**
 * @author Emmanuel Bernard
 */

@Embeddable
public class Contact {
	@NotNull
	private String name;
	@NotNull
	private String phone;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}