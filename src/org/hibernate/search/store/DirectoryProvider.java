//$Id: $
package org.hibernate.search.store;

import java.util.Properties;

import org.apache.lucene.store.Directory;


/**
 * Set up and provide a Lucene <code>Directory</code>
 * <code>equals()</code> and <code>hashCode()</code> must guaranty equality
 * between two providers pointing to the same underlying Lucene Store
 * This class must be thread safe regarding <code>getDirectory()</code>
 * calls
 *
 * @author Emmanuel Bernard
 * @author Sylvain Vieujot
 */
public interface DirectoryProvider<TDirectory extends Directory> {
	/**
	 * get the information to initialize the directory and build its hashCode
	 */
	void initialize(String directoryProviderName, Properties properties);

	/**
	 * Returns an initialized Lucene Directory. This method call <b>must</b> be threadsafe
	 */
	TDirectory getDirectory();
}

