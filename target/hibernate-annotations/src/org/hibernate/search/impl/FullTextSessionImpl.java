//$Id: $
package org.hibernate.search.impl;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.transaction.Status;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.query.ParameterMetadata;
import org.hibernate.impl.SessionImpl;
import org.hibernate.search.query.FullTextQueryImpl;
import org.hibernate.search.event.FullTextIndexEventListener;
import org.hibernate.search.util.ContextHelper;
import org.hibernate.search.engine.DocumentBuilder;
import org.hibernate.search.backend.UpdateWork;
import org.hibernate.search.backend.Work;
import org.hibernate.search.backend.WorkQueue;
import org.hibernate.search.backend.impl.BatchLuceneWorkQueue;
import org.hibernate.search.backend.impl.PostTransactionWorkQueueSynchronization;
import org.hibernate.search.store.DirectoryProvider;
import org.hibernate.search.FullTextSession;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.type.Type;
import org.apache.lucene.document.Document;

/**
 * Lucene Full text search aware session
 *
 * @author Emmanuel Bernard
 */
public class FullTextSessionImpl implements FullTextSession {
	private final SessionImpl session;
	private PostTransactionWorkQueueSynchronization postTransactionWorkQueueSynch;

	public FullTextSessionImpl(Session session) {
		this.session = (SessionImpl) session;
	}

	/**
	 * Execute a Lucene query and retrieve managed objects of type entities (or their indexed subclasses)
     * If entities is empty, include all indexed entities
     * 
	 * @param entities must be immutable for the lifetime of the query object
	 */
	public Query createFullTextQuery(org.apache.lucene.search.Query luceneQuery, Class... entities) {
		return new FullTextQueryImpl( luceneQuery, entities, session, new ParameterMetadata(null, null) );
	}

	/**
	 * (re)index an entity.
	 * Non indexable entities are ignored
	 * The entity must be associated with the session
	 *
	 * @param entity must not be null
	 */
	public void index(Object entity) {
		if (entity == null) return;
		Class clazz = entity.getClass();
		FullTextIndexEventListener listener = ContextHelper.getLuceneEventListener( session );
		DocumentBuilder<Object> builder = listener.getDocumentBuilders().get( clazz );
		if ( builder != null ) {
			Serializable id = session.getIdentifier( entity );
			Document doc = builder.getDocument( entity, id );
			UpdateWork work = new UpdateWork( id, entity.getClass(), doc );
			processWork( work, listener.getDocumentBuilders(), listener.getLockableDirectoryProviders() );
		}
		//TODO
		//need to add elements in a queue kept at the Session level
		//the queue will be processed by a Lucene(Auto)FlushEventListener
		//note that we could keep this queue somewhere in the event listener in the mean time but that requires
		// a synchronized hashmap holding this queue on a per session basis plus some session house keeping (yuk)
		//an other solution would be to subclass SessionImpl instead of having this LuceneSession delecation model
		// this is an open discussion
	}

	private void processWork(Work work, Map<Class, DocumentBuilder<Object>> documentBuilders,
							 Map<DirectoryProvider, ReentrantLock> lockableDirectoryProviders) {
		if ( session.isTransactionInProgress() ) {
			if ( postTransactionWorkQueueSynch == null || postTransactionWorkQueueSynch.isConsumed() ) {
				postTransactionWorkQueueSynch = createWorkQueueSync( documentBuilders, lockableDirectoryProviders);
				session.getTransaction().registerSynchronization( postTransactionWorkQueueSynch );
			}
			postTransactionWorkQueueSynch.add( work );
		}
		else {
			//no transaction work right away
			PostTransactionWorkQueueSynchronization sync =
					createWorkQueueSync( documentBuilders, lockableDirectoryProviders );
			sync.add( work );
			sync.afterCompletion( Status.STATUS_COMMITTED );
		}
	}

	private PostTransactionWorkQueueSynchronization createWorkQueueSync(
			Map<Class, DocumentBuilder<Object>> documentBuilders,
			Map<DirectoryProvider, ReentrantLock> lockableDirectoryProviders) {
		WorkQueue workQueue = new BatchLuceneWorkQueue( documentBuilders, lockableDirectoryProviders );
		return new PostTransactionWorkQueueSynchronization( workQueue );
	}

	public Query createSQLQuery(String sql, String returnAlias, Class returnClass) {
		return session.createSQLQuery( sql, returnAlias, returnClass );
	}

	public Query createSQLQuery(String sql, String[] returnAliases, Class[] returnClasses) {
		return session.createSQLQuery( sql, returnAliases, returnClasses );
	}

	public int delete(String query) throws HibernateException {
		return session.delete( query );
	}

	public int delete(String query, Object value, Type type) throws HibernateException {
		return session.delete( query, value, type );
	}

	public int delete(String query, Object[] values, Type[] types) throws HibernateException {
		return session.delete( query, values, types );
	}

	public Collection filter(Object collection, String filter) throws HibernateException {
		return session.filter( collection, filter );
	}

	public Collection filter(Object collection, String filter, Object value, Type type) throws HibernateException {
		return session.filter( collection, filter, value, type );
	}

	public Collection filter(Object collection, String filter, Object[] values, Type[] types) throws HibernateException {
		return session.filter( collection, filter, values, types );
	}

	public List find(String query) throws HibernateException {
		return session.find( query );
	}

	public List find(String query, Object value, Type type) throws HibernateException {
		return session.find( query, value, type );
	}

	public List find(String query, Object[] values, Type[] types) throws HibernateException {
		return session.find( query, values, types );
	}

	public Iterator iterate(String query) throws HibernateException {
		return session.iterate( query );
	}

	public Iterator iterate(String query, Object value, Type type) throws HibernateException {
		return session.iterate( query, value, type );
	}

	public Iterator iterate(String query, Object[] values, Type[] types) throws HibernateException {
		return session.iterate( query, values, types );
	}

	public void save(String entityName, Object object, Serializable id) throws HibernateException {
		session.save( entityName, object, id );
	}

	public void save(Object object, Serializable id) throws HibernateException {
		session.save( object, id );
	}

	public Object saveOrUpdateCopy(String entityName, Object object) throws HibernateException {
		return session.saveOrUpdateCopy( entityName, object );
	}

	public Object saveOrUpdateCopy(String entityName, Object object, Serializable id) throws HibernateException {
		return session.saveOrUpdateCopy( entityName, object, id );
	}

	public Object saveOrUpdateCopy(Object object) throws HibernateException {
		return session.saveOrUpdateCopy( object );
	}

	public Object saveOrUpdateCopy(Object object, Serializable id) throws HibernateException {
		return session.saveOrUpdateCopy( object, id );
	}

	public void update(String entityName, Object object, Serializable id) throws HibernateException {
		session.update( entityName, object, id );
	}

	public void update(Object object, Serializable id) throws HibernateException {
		session.update( object, id );
	}

	public Transaction beginTransaction() throws HibernateException {
		return session.beginTransaction();
	}

	public void cancelQuery() throws HibernateException {
		session.cancelQuery();
	}

	public void clear() {
		if (postTransactionWorkQueueSynch != null && !postTransactionWorkQueueSynch.isConsumed() ) {
			postTransactionWorkQueueSynch.afterCompletion( Status.STATUS_ROLLEDBACK );
		}
		session.clear();
	}

	public Connection close() throws HibernateException {
		return session.close();
	}

	public Connection connection() throws HibernateException {
		return session.connection();
	}

	public boolean contains(Object object) {
		return session.contains( object );
	}

	public Criteria createCriteria(String entityName) {
		return session.createCriteria( entityName );
	}

	public Criteria createCriteria(String entityName, String alias) {
		return session.createCriteria( entityName, alias );
	}

	public Criteria createCriteria(Class persistentClass) {
		return session.createCriteria( persistentClass );
	}

	public Criteria createCriteria(Class persistentClass, String alias) {
		return session.createCriteria( persistentClass, alias );
	}

	public Query createFilter(Object collection, String queryString) throws HibernateException {
		return session.createFilter( collection, queryString );
	}

	public Query createQuery(String queryString) throws HibernateException {
		return session.createQuery( queryString );
	}

	public SQLQuery createSQLQuery(String queryString) throws HibernateException {
		return session.createSQLQuery( queryString );
	}

	public void delete(String entityName, Object object) throws HibernateException {
		session.delete( entityName, object );
	}

	public void delete(Object object) throws HibernateException {
		session.delete( object );
	}

	public void disableFilter(String filterName) {
		session.disableFilter( filterName );
	}

	public Connection disconnect() throws HibernateException {
		return session.disconnect();
	}

	public Filter enableFilter(String filterName) {
		return session.enableFilter( filterName );
	}

	public void evict(Object object) throws HibernateException {
		session.evict( object );
	}

	public void flush() throws HibernateException {
		session.flush();
	}

	public Object get(Class clazz, Serializable id) throws HibernateException {
		return session.get( clazz, id );
	}

	public Object get(Class clazz, Serializable id, LockMode lockMode) throws HibernateException {
		return session.get( clazz, id, lockMode );
	}

	public Object get(String entityName, Serializable id) throws HibernateException {
		return session.get( entityName, id );
	}

	public Object get(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
		return session.get( entityName, id, lockMode );
	}

	public CacheMode getCacheMode() {
		return session.getCacheMode();
	}

	public LockMode getCurrentLockMode(Object object) throws HibernateException {
		return session.getCurrentLockMode( object );
	}

	public Filter getEnabledFilter(String filterName) {
		return session.getEnabledFilter( filterName );
	}

	public EntityMode getEntityMode() {
		return session.getEntityMode();
	}

	public String getEntityName(Object object) throws HibernateException {
		return session.getEntityName( object );
	}

	public FlushMode getFlushMode() {
		return session.getFlushMode();
	}

	public Serializable getIdentifier(Object object) throws HibernateException {
		return session.getIdentifier( object );
	}

	public Query getNamedQuery(String queryName) throws HibernateException {
		return session.getNamedQuery( queryName );
	}

	public org.hibernate.Session getSession(EntityMode entityMode) {
		return session.getSession( entityMode );
	}

	public SessionFactory getSessionFactory() {
		return session.getSessionFactory();
	}

	public SessionStatistics getStatistics() {
		return session.getStatistics();
	}

	public Transaction getTransaction() {
		return session.getTransaction();
	}

	public boolean isConnected() {
		return session.isConnected();
	}

	public boolean isDirty() throws HibernateException {
		return session.isDirty();
	}

	public boolean isOpen() {
		return session.isOpen();
	}

	public Object load(String entityName, Serializable id) throws HibernateException {
		return session.load( entityName, id );
	}

	public Object load(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
		return session.load( entityName, id, lockMode );
	}

	public void load(Object object, Serializable id) throws HibernateException {
		session.load( object, id );
	}

	public Object load(Class theClass, Serializable id) throws HibernateException {
		return session.load( theClass, id );
	}

	public Object load(Class theClass, Serializable id, LockMode lockMode) throws HibernateException {
		return session.load( theClass, id, lockMode );
	}

	public void lock(String entityName, Object object, LockMode lockMode) throws HibernateException {
		session.lock( entityName, object, lockMode );
	}

	public void lock(Object object, LockMode lockMode) throws HibernateException {
		session.lock( object, lockMode );
	}

	public Object merge(String entityName, Object object) throws HibernateException {
		return session.merge( entityName, object );
	}

	public Object merge(Object object) throws HibernateException {
		return session.merge( object );
	}

	public void persist(String entityName, Object object) throws HibernateException {
		session.persist( entityName, object );
	}

	public void persist(Object object) throws HibernateException {
		session.persist( object );
	}

	public void reconnect() throws HibernateException {
		session.reconnect();
	}

	public void reconnect(Connection connection) throws HibernateException {
		session.reconnect( connection );
	}

	public void refresh(Object object) throws HibernateException {
		session.refresh( object );
	}

	public void refresh(Object object, LockMode lockMode) throws HibernateException {
		session.refresh( object, lockMode );
	}

	public void replicate(String entityName, Object object, ReplicationMode replicationMode) throws HibernateException {
		session.replicate( entityName, object, replicationMode );
	}

	public void replicate(Object object, ReplicationMode replicationMode) throws HibernateException {
		session.replicate( object, replicationMode );
	}

	public Serializable save(String entityName, Object object) throws HibernateException {
		return session.save( entityName, object );
	}

	public Serializable save(Object object) throws HibernateException {
		return session.save( object );
	}

	public void saveOrUpdate(String entityName, Object object) throws HibernateException {
		session.saveOrUpdate( entityName, object );
	}

	public void saveOrUpdate(Object object) throws HibernateException {
		session.saveOrUpdate( object );
	}

	public void setCacheMode(CacheMode cacheMode) {
		session.setCacheMode( cacheMode );
	}

	public void setFlushMode(FlushMode flushMode) {
		session.setFlushMode( flushMode );
	}

	public void setReadOnly(Object entity, boolean readOnly) {
		session.setReadOnly( entity, readOnly );
	}

	public void update(String entityName, Object object) throws HibernateException {
		session.update( entityName, object );
	}

	public void update(Object object) throws HibernateException {
		session.update( object );
	}
}
