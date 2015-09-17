//$Id: Address.java 8974 2006-01-04 00:43:49Z epbernard $
package org.hibernate.test.annotations.embedded;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 * @author Emmanuel Bernard
 */
@Embeddable
public class Address implements Serializable {
	String address1;
	@Column(name = "fld_city")
	String city;
	Country country;
	@ManyToOne
	AddressType type;
}
