//$Id: Noise.java 9044 2006-01-13 01:58:41Z epbernard $
package org.hibernate.test.annotations.inheritance.singletable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
@DiscriminatorValue("0")
public class Noise extends Music {
}
