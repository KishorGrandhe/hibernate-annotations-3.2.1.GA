//$Id: Language.java 9316 2006-02-22 20:47:31Z epbernard $
package org.hibernate.test.reflection.java.generics;

/**
 * @author Emmanuel Bernard
 */
public interface Language<T> {
	T getLanguage();
}
