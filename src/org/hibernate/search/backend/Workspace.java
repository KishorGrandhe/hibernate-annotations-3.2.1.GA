//$Id: $
package org.hibernate.search.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.hibernate.HibernateException;
import org.hibernate.search.engine.DocumentBuilder;
import org.hibernate.search.store.DirectoryProvider;

/**
 * Lucene workspace
 * This is not intended to be used in a multithreaded environment
 * <p/>
 * One cannot execute modification through an IndexReader when an IndexWriter has been acquired on the same underlying directory
 * One cannot get an IndexWriter when an IndexReader have been acquired and modificed on the same underlying directory
 * The recommended approach is to execute all the modifications on the IndexReaders, {@link #clean()} }, and acquire the
 * index writers
 *
 * @author Emmanuel Bernard
 */
//TODO introduce the notion of read only IndexReader? We cannot enforce it because Lucene use abstract classes, not interfaces
public class Workspace {
	private static Log log = LogFactory.getLog( Workspace.class );
	private Map<Class, DocumentBuilder<Object>> documentBuilders;
	private Map<DirectoryProvider, ReentrantLock> lockableDirectoryProviders;
	private Map<DirectoryProvider, IndexReader> readers = new HashMap<DirectoryProvider, IndexReader>();
	private Map<DirectoryProvider, IndexWriter> writers = new HashMap<DirectoryProvider, IndexWriter>();
	private List<DirectoryProvider> lockedProviders = new ArrayList<DirectoryProvider>();

	public Workspace(Map<Class, DocumentBuilder<Object>> documentBuilders,
					 Map<DirectoryProvider, ReentrantLock> lockableDirectoryProviders) {
		this.documentBuilders = documentBuilders;
		this.lockableDirectoryProviders = lockableDirectoryProviders;
	}


	public DocumentBuilder getDocumentBuilder(Class entity) {
		return documentBuilders.get( entity );
	}

	public IndexReader getIndexReader(Class entity) {
		//TODO NPEs
		DirectoryProvider provider = documentBuilders.get( entity ).getDirectoryProvider();
		IndexReader reader = readers.get( provider );
		if ( reader != null ) return reader;
		lockProvider( provider );
		try {
			reader = IndexReader.open( provider.getDirectory() );
			readers.put( provider, reader );
		}
		catch (IOException e) {
			cleanUp( new HibernateException( "Unable to open IndexReader for " + entity, e ) );
		}
		return reader;
	}

	public IndexWriter getIndexWriter(Class entity) {
		DirectoryProvider provider = documentBuilders.get( entity ).getDirectoryProvider();
		IndexWriter writer = writers.get( provider );
		if ( writer != null ) return writer;
		lockProvider( provider );
		try {
			writer = new IndexWriter(
					provider.getDirectory(), documentBuilders.get( entity ).getAnalyzer(), false
			); //have been created at init time
			writers.put( provider, writer );
		}
		catch (IOException e) {
			cleanUp( new HibernateException( "Unable to open IndexWriter for " + entity, e ) );
		}
		return writer;
	}

	private void lockProvider(DirectoryProvider provider) {
		//make sure to use a semaphore
		ReentrantLock lock = lockableDirectoryProviders.get( provider );
		//of course a given thread cannot have a race cond with itself
		if ( !lock.isHeldByCurrentThread() ) {
			lock.lock();
			lockedProviders.add( provider );
		}
	}

	private void cleanUp(HibernateException originalException) {
		//release all readers and writers, then reelase locks
		HibernateException raisedException = originalException;
		for ( IndexReader reader : readers.values() ) {
			try {
				reader.close();
			}
			catch (IOException e) {
				if ( raisedException != null ) {
					log.error( "Subsequent Exception while closing IndexReader", e );
				}
				else {
					raisedException = new HibernateException( "Exception while closing IndexReader", e );
				}
			}
		}
		for ( IndexWriter writer : writers.values() ) {
			try {
				writer.close();
			}
			catch (IOException e) {
				if ( raisedException != null ) {
					log.error( "Subsequent Exception while closing IndexWriter", e );
				}
				else {
					raisedException = new HibernateException( "Exception while closing IndexWriter", e );
				}
			}
		}
		for ( DirectoryProvider provider : lockedProviders ) {
			lockableDirectoryProviders.get( provider ).unlock();
		}
		readers.clear();
		writers.clear();
		lockedProviders.clear();
		if ( raisedException != null ) throw raisedException;
	}

	/**
	 * release resources consumed in the workspace if any
	 */
	public void clean() {
		cleanUp( null );
	}
}
