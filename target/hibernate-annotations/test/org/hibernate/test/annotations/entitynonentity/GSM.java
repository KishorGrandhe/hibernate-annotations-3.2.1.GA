//$Id: GSM.java 8980 2006-01-04 22:57:22Z epbernard $
package org.hibernate.test.annotations.entitynonentity;

import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class GSM extends Cellular {
	int frequency;
}
