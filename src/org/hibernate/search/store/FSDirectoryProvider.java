//$Id: $
package org.hibernate.search.store;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.hibernate.HibernateException;

/**
 * Use a Lucene FSDirectory
 * The base directory is represented by hibernate.search.<index>.indexBase
 * The index is created in <base directory>/<index name>
 *
 * @author Emmanuel Bernard
 * @author Sylvain Vieujot
 */
public class FSDirectoryProvider implements DirectoryProvider<FSDirectory> {
	private FSDirectory directory;
	private static Log log = LogFactory.getLog( FSDirectoryProvider.class );
	private String indexName;

	public void initialize(String directoryProviderName, Properties properties) {
		String indexBase = properties.getProperty( "indexBase", "." );
		File indexDir = new File( indexBase );

		if ( !( indexDir.exists() && indexDir.isDirectory() ) ) {
			//TODO create the directory?
			throw new HibernateException( MessageFormat.format( "Index directory does not exists: {0}", indexBase ) );
		}
		if ( !indexDir.canWrite() ) {
			throw new HibernateException( "Cannot write into index directory: " + indexBase );
		}
		log.info( "Setting index dir to " + indexDir );

		File file = new File( indexDir, directoryProviderName );

		try {
			boolean create = !file.exists();
			indexName = file.getCanonicalPath();
			directory = FSDirectory.getDirectory( indexName, create );
			if ( create ) {
				IndexWriter iw = new IndexWriter( directory, new StandardAnalyzer(), create );
				iw.close();
			}
		}
		catch (IOException e) {
			throw new HibernateException( "Unable to initialize index: " + directoryProviderName, e );
		}

	}

	public FSDirectory getDirectory() {
		return directory;
	}

	@Override
	public boolean equals(Object obj) {
		// this code is actually broken since the value change after initialize call
		// but from a practical POV this is fine since we only call this method
		// after initialize call
		if ( obj == this ) return true;
		if ( obj == null || !( obj instanceof FSDirectoryProvider ) ) return false;
		return indexName.equals( ( (FSDirectoryProvider) obj ).indexName );
	}

	@Override
	public int hashCode() {
		// this code is actually broken since the value change after initialize call
		// but from a practical POV this is fine since we only call this method
		// after initialize call
		return indexName.hashCode();
	}
}
