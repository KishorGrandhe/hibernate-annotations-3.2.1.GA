//$Id: Sweater.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.inheritance.joined;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

/**
 * @author Emmanuel Bernard
 */
@Entity
@PrimaryKeyJoinColumn(name = "clothing_id")
public class Sweater extends Clothing {
	private boolean isSweat;

	public boolean isSweat() {
		return isSweat;
	}

	public void setSweat(boolean sweat) {
		isSweat = sweat;
	}
}
