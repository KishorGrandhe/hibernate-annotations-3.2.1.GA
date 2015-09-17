//$Id: Environment.java 10742 2006-11-07 01:03:16Z epbernard $
package org.hibernate.search;

/**
 * @author Emmanuel Bernard
 */
public final class Environment {
	/**
	 * Indexes base directory
	 */
	public static final String INDEX_BASE_DIR = "hibernate.search.index_dir";

	/**
	 * Lucene analyser
	 */
	public static final String ANALYZER_CLASS = "hibernate.search.analyzer";
}
