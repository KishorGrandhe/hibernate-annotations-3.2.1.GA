//$Id: PolymorphismType.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.annotations;

/**
 * Type of avaliable polymorphism for a particular entity
 *
 * @author Emmanuel Bernard
 */
public enum PolymorphismType {
	/**
	 * default, this entity is retrieved if any of its super entity is asked
	 */
	IMPLICIT,
	/**
	 * this entity is retrived only if explicitly asked
	 */
	EXPLICIT
}