//$Id: Phone.java 8982 2006-01-05 13:49:51Z epbernard $
package org.hibernate.test.annotations.entitynonentity;

import javax.persistence.MappedSuperclass;

/**
 * @author Emmanuel Bernard
 */
@MappedSuperclass
public class Phone extends Voice {
	boolean isNumeric;
}
