//$Id: Flight.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.duplicatedgenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Here to test duplicate import
 *
 * @author Emmanuel Bernard
 */
@Entity
@Table(name = "tbl_flight")
public class Flight {
	@Id
	public String id;
}
