//$Id: Paper.java 8458 2005-10-25 22:50:20Z epbernard $
package org.hibernate.test.annotations.generics;

import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Paper extends Item<PaperType, SomeGuy> {
}
