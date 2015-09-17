//$Id: Currency.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.notfound;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Currency implements Serializable {
	private Integer id;
	private String name;

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
