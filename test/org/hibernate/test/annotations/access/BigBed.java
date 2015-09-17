//$Id: BigBed.java 8974 2006-01-04 00:43:49Z epbernard $
package org.hibernate.test.annotations.access;

import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class BigBed extends Bed {
	public int size;
}
