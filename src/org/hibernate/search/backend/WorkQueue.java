//$Id: $
package org.hibernate.search.backend;

/**
 * Set of work operations
 *
 * @author Emmanuel Bernard
 */
public interface WorkQueue {
	/**
	 * Add a work
	 */
	void add(Work work);

	/**
	 * Execute works
	 */
	void performWork();

	/**
	 * Rollback works
	 */
	void cancelWork();
}
