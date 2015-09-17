//$Id: Shoe.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.id;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * sample of Sequance generator
 *
 * @author Emmanuel Bernard
 */
@Entity
public class Shoe implements Serializable {
	private Long id;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GEN")
	public Long getId() {
		return id;
	}

	public void setId(Long long1) {
		id = long1;
	}
}
