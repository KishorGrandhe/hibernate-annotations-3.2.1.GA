//$Id: Deal.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.embedded;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * @author Emmanuel Bernard
 */
@MappedSuperclass
public class Deal {
	/**
	 * Deal ID.
	 */
	private String id;

	@Id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Swap with the tenor.
	 */
	private Swap swap;

	@Embedded
	public Swap getSwap() {
		return swap;
	}

	public void setSwap(Swap swap) {
		this.swap = swap;
	}
}
