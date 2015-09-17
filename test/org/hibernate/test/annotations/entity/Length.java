//$Id: Length.java 6760 2005-05-12 13:34:04Z epbernard $
package org.hibernate.test.annotations.entity;

/**
 * @author Emmanuel Bernard
 */
public interface Length<Type> {
	Type getLength();
}
