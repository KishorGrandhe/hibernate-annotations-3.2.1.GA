//$Id: Filter.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.reflection;

/**
 * Filter properties
 *
 * @author Emmanuel Bernard
 */
public interface Filter {
	boolean returnStatic();

	boolean returnTransient();
}
