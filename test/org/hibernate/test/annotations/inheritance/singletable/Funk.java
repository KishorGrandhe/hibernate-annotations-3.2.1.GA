//$Id: Funk.java 9044 2006-01-13 01:58:41Z epbernard $
package org.hibernate.test.annotations.inheritance.singletable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
@DiscriminatorValue("1")
public class Funk extends Music {
	private int starred;

	public int getStarred() {
		return starred;
	}

	public void setStarred(int starred) {
		this.starred = starred;
	}
}
