//$Id: FullTextIndexEventListener.java 10865 2006-11-23 23:30:01 +0100 (jeu., 23 nov. 2006) epbernard $
package org.hibernate.search.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;
import javax.transaction.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.Initializable;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.search.Environment;
import org.hibernate.search.util.WeakIdentityHashMap;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.backend.AddWork;
import org.hibernate.search.backend.DeleteWork;
import org.hibernate.search.backend.UpdateWork;
import org.hibernate.search.backend.Work;
import org.hibernate.search.backend.WorkQueue;
import org.hibernate.search.backend.impl.BatchLuceneWorkQueue;
import org.hibernate.search.backend.impl.PostTransactionWorkQueueSynchronization;
import org.hibernate.search.engine.DocumentBuilder;
import org.hibernate.search.store.DirectoryProvider;
import org.hibernate.search.store.DirectoryProviderFactory;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.reflection.ReflectionManager;
import org.hibernate.reflection.XClass;
import org.hibernate.util.ReflectHelper;

/**
 * This listener supports setting a parent directory for all generated index files.
 * It also supports setting the analyzer class to be used.
 *
 * @author Gavin King
 * @author Emmanuel Bernard
 * @author Mattias Arbin
 */
//TODO work on sharing the same indexWriters and readers across a single post operation...
//TODO implement and use a LockableDirectoryProvider that wraps a DP to handle the lock inside the LDP
public class FullTextIndexEventListener implements PostDeleteEventListener, PostInsertEventListener,
		PostUpdateEventListener, Initializable {
	protected ReflectionManager reflectionManager;
	//not a synchronized map since for a given transaction, we have not concurrent access
	protected WeakIdentityHashMap queuePerTransaction;

	//FIXME keeping this here is a bad decision since you might want to search indexes wo maintain it
	@Deprecated
	public Map<Class, DocumentBuilder<Object>> getDocumentBuilders() {
		return documentBuilders;
	}


	private Map<Class, DocumentBuilder<Object>> documentBuilders = new HashMap<Class, DocumentBuilder<Object>>();
	//keep track of the index modifiers per DirectoryProvider since multiple entity can use the same directory provider
	private Map<DirectoryProvider, ReentrantLock> lockableDirectoryProviders =
			new HashMap<DirectoryProvider, ReentrantLock>();
	private boolean initialized;

	private static final Log log = LogFactory.getLog( FullTextIndexEventListener.class );

	public void initialize(Configuration cfg) {
		if ( initialized ) return;
		//yuk
		reflectionManager = ( (AnnotationConfiguration) cfg ).createExtendedMappings().getReflectionManager();
		queuePerTransaction = new WeakIdentityHashMap();

		Class analyzerClass;
		String analyzerClassName = cfg.getProperty( Environment.ANALYZER_CLASS );
		if ( analyzerClassName != null ) {
			try {
				analyzerClass = ReflectHelper.classForName( analyzerClassName );
			}
			catch (Exception e) {
				throw new HibernateException(
						"Lucene analyzer class '" + analyzerClassName + "' defined in property '" + Environment.ANALYZER_CLASS + "' could not be found.",
						e
				);
			}
		}
		else {
			analyzerClass = StandardAnalyzer.class;
		}
		// Initialize analyzer
		Analyzer analyzer;
		try {
			analyzer = (Analyzer) analyzerClass.newInstance();
		}
		catch (ClassCastException e) {
			throw new HibernateException(
					"Lucene analyzer does not implement " + Analyzer.class.getName() + ": " + analyzerClassName
			);
		}
		catch (Exception e) {
			throw new HibernateException( "Failed to instantiate lucene analyzer with type " + analyzerClassName );
		}

		Iterator iter = cfg.getClassMappings();
		DirectoryProviderFactory factory = new DirectoryProviderFactory();
		while ( iter.hasNext() ) {
			PersistentClass clazz = (PersistentClass) iter.next();
			Class<?> mappedClass = clazz.getMappedClass();
			if ( mappedClass != null ) {
				XClass mappedXClass = reflectionManager.toXClass( mappedClass );
				if ( mappedXClass != null && mappedXClass.isAnnotationPresent( Indexed.class ) ) {
					DirectoryProvider provider = factory.createDirectoryProvider( mappedXClass, cfg );
					if ( !lockableDirectoryProviders.containsKey( provider ) ) {
						lockableDirectoryProviders.put( provider, new ReentrantLock() );
					}
					final DocumentBuilder<Object> documentBuilder = new DocumentBuilder<Object>(
							mappedXClass, analyzer, provider, reflectionManager
					);

					documentBuilders.put( mappedClass, documentBuilder );
				}
			}
		}
		Set<Class> indexedClasses = documentBuilders.keySet();
		for ( DocumentBuilder builder : documentBuilders.values() ) {
			builder.postInitialize( indexedClasses );
		}
		initialized = true;
	}

	public void onPostDelete(PostDeleteEvent event) {
		if ( documentBuilders.containsKey( event.getEntity().getClass() ) ) {
			DeleteWork work = new DeleteWork( event.getId(), event.getEntity().getClass() );
			processWork( work, event );
		}
	}

	public void onPostInsert(PostInsertEvent event) {
		final Object entity = event.getEntity();
		DocumentBuilder<Object> builder = documentBuilders.get( entity.getClass() );
		if ( builder != null ) {
			Serializable id = event.getId();
			Document doc = builder.getDocument( entity, id );
			AddWork work = new AddWork( id, entity.getClass(), doc );
			processWork( work, event );
		}
	}

	public void onPostUpdate(PostUpdateEvent event) {
		final Object entity = event.getEntity();
		DocumentBuilder<Object> builder = documentBuilders.get( entity.getClass() );
		if ( builder != null ) {
			Serializable id = event.getId();
			Document doc = builder.getDocument( entity, id );
			UpdateWork work = new UpdateWork( id, entity.getClass(), doc );
			processWork( work, event );
		}
	}

	private void processWork(Work work, AbstractEvent event) {
		if ( event.getSession().isTransactionInProgress() ) {
			Transaction transaction = event.getSession().getTransaction();
			PostTransactionWorkQueueSynchronization sync = (PostTransactionWorkQueueSynchronization)
					queuePerTransaction.get( transaction );
			if ( sync == null || sync.isConsumed() ) {
				WorkQueue workQueue = new BatchLuceneWorkQueue( documentBuilders, lockableDirectoryProviders );
				sync = new PostTransactionWorkQueueSynchronization( workQueue, queuePerTransaction );
				transaction.registerSynchronization( sync );
				queuePerTransaction.put(transaction, sync);
			}
			sync.add( work );
		}
		else {
			WorkQueue workQueue = new BatchLuceneWorkQueue( documentBuilders, lockableDirectoryProviders );
			PostTransactionWorkQueueSynchronization sync = new PostTransactionWorkQueueSynchronization( workQueue );
			sync.add( work );
			sync.afterCompletion( Status.STATUS_COMMITTED );
		}
	}

	public Map<DirectoryProvider, ReentrantLock> getLockableDirectoryProviders() {
		return lockableDirectoryProviders;
	}
}
