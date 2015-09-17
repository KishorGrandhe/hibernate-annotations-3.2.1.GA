//$Id: Voice.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.entitynonentity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author Emmanuel Bernard
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Voice extends Communication {
	@Id
	@GeneratedValue
	public Integer id;
}
