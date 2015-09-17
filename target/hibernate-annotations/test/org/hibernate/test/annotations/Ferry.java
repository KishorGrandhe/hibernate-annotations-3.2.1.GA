//$Id: Ferry.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations;

import javax.persistence.Entity;


/**
 * @author Emmanuel Bernard
 */
@Entity()
public class Ferry extends Boat {
	private String sea;

	public String getSea() {
		return sea;
	}

	public void setSea(String string) {
		sea = string;
	}

}
