//$Id: Store.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.id;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Sample of class generator
 *
 * @author Emmanuel Bernard
 */
@Entity
@javax.persistence.SequenceGenerator(
		name = "SEQ_STORE",
		sequenceName = "my_sequence"
)
public class Store implements Serializable {
	private Long id;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STORE")
	public Long getId() {
		return id;
	}

	public void setId(Long long1) {
		id = long1;
	}
}
