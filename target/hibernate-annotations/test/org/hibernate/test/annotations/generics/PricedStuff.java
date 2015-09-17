//$Id: PricedStuff.java 8982 2006-01-05 13:49:51Z epbernard $
package org.hibernate.test.annotations.generics;

import javax.persistence.MappedSuperclass;

/**
 * @author Emmanuel Bernard
 */
@MappedSuperclass
public class PricedStuff extends Stuff<Price> {
}
