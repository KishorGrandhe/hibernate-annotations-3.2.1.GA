//$Id: Ball.java 10276 2006-08-17 16:07:03Z epbernard $
package org.hibernate.test.annotations.id;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

/**
 * Sample of table generator
 *
 * @author Emmanuel Bernard
 */
@TableGenerator(name = "EMP_GEN", table = "GENERATOR_TABLE", pkColumnName = "pkey",
		valueColumnName = "hi", pkColumnValue = "Ball", allocationSize = 10)
@Entity
public class Ball implements Serializable {
	private Integer id;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "EMP_GEN")
	@Column(name = "ball_id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
