//$Id: $
package org.hibernate.search.query;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.query.ParameterMetadata;
import org.hibernate.impl.AbstractQueryImpl;
import org.hibernate.search.engine.DocumentBuilder;
import org.hibernate.search.event.FullTextIndexEventListener;
import org.hibernate.search.util.ContextHelper;

/**
 * @author Emmanuel Bernard
 */
//TODO implements setParameter()
public class FullTextQueryImpl extends AbstractQueryImpl {
	private static final Log log = LogFactory.getLog( FullTextQueryImpl.class );
	private org.apache.lucene.search.Query luceneQuery;
	private Class[] classes;
	private Set<Class> classesAndSubclasses;
	private Integer firstResult;
	private Integer maxResults;
	private int resultSize;

	/**
	 * classes must be immutable
	 */
	public FullTextQueryImpl(org.apache.lucene.search.Query query, Class[] classes, SessionImplementor session,
						   ParameterMetadata parameterMetadata) {
		//TODO handle flushMode
		super( query.toString(), null, session, parameterMetadata );
		this.luceneQuery = query;
		this.classes = classes;
	}

	/**
	 * Return an interator on the results.
	 * Retrieve the object one by one (initialize it during the next() operation)
	 */
	public Iterator iterate() throws HibernateException {
		//implement an interator which keep the id/class for each hit and get the object on demand
		//cause I can't keep the searcher and hence the hit opened. I dont have any hook to know when the
		//user stop using it
		//scrollable is better in this area

		FullTextIndexEventListener listener = ContextHelper.getLuceneEventListener( session );
		//find the directories
		Searcher searcher = buildSearcher( listener );
		try {
			org.apache.lucene.search.Query query = filterQueryByClasses( luceneQuery );
			Hits hits = searcher.search( query );
			setResultSize( hits );
			int first = first();
			int max = max( first, hits );
			List<EntityInfo> entityInfos = new ArrayList<EntityInfo>( max - first + 1 );
			for ( int index = first; index <= max; index++ ) {
				Document document = hits.doc( index );
				EntityInfo entityInfo = new EntityInfo();
				entityInfo.clazz = DocumentBuilder.getDocumentClass( document );
				entityInfo.id = DocumentBuilder.getDocumentId( listener, entityInfo.clazz, document );
				entityInfos.add( entityInfo );
			}
			return new IteratorImpl( entityInfos, (Session) this.session );
		}
		catch (IOException e) {
			throw new HibernateException( "Unable to query Lucene index", e );
		}
		finally {
			if ( searcher != null ) try {
				searcher.close();
			}
			catch (IOException e) {
				log.warn( "Unable to properly close searcher during lucene query: " + getQueryString(), e );
			}
		}
	}

		public ScrollableResults scroll() throws HibernateException {
		//keep the searcher open until the resultset is closed
		FullTextIndexEventListener listener = ContextHelper.getLuceneEventListener( session );;
		//find the directories
		Searcher searcher = buildSearcher( listener );
		Hits hits;
		try {
			org.apache.lucene.search.Query query = filterQueryByClasses( luceneQuery );
			hits = searcher.search( query );
			setResultSize( hits );
			int first = first();
			int max = max( first, hits );
			return new ScrollableResultsImpl( searcher, hits, first, max, (Session) this.session, listener );
		}
		catch (IOException e) {
			try {
				if ( searcher != null ) searcher.close();
			}
			catch (IOException ee) {
				//we have the initial issue already
			}
			throw new HibernateException( "Unable to query Lucene index", e );
		}
	}

	public ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException {
		//TODO think about this scrollmode
		return scroll();
	}

	public List list() throws HibernateException {
		FullTextIndexEventListener listener = ContextHelper.getLuceneEventListener( session );;
		//find the directories
		Searcher searcher = buildSearcher( listener );
		Hits hits;
		try {
			org.apache.lucene.search.Query query = filterQueryByClasses( luceneQuery );
			hits = searcher.search( query );
			setResultSize( hits );
			int first = first();
			int max = max( first, hits );
			List result = new ArrayList( max - first + 1 );
			Session sess = (Session) this.session;
			for ( int index = first; index <= max; index++ ) {
				Document document = hits.doc( index );
				Class clazz = DocumentBuilder.getDocumentClass( document );
				Serializable id = DocumentBuilder.getDocumentId( listener, clazz, document );
				result.add( sess.load( clazz, id ) );
				//use load to benefit from the batch-size
				//we don't face proxy casting issues since the exact class is extracted from the index
			}
			//then initialize the objects
			for ( Object element : result ) {
				Hibernate.initialize( element );
			}
			return result;
		}
		catch (IOException e) {
			throw new HibernateException( "Unable to query Lucene index", e );
		}
		finally {
			if ( searcher != null ) try {
				searcher.close();
			}
			catch (IOException e) {
				log.warn( "Unable to properly close searcher during lucene query: " + getQueryString(), e );
			}
		}
	}

	private org.apache.lucene.search.Query filterQueryByClasses(org.apache.lucene.search.Query luceneQuery) {
		//A query filter is more practical than a manual class filtering post query (esp on scrollable resultsets)
		//it also probably minimise the memory footprint
		if ( classesAndSubclasses == null ) {
			return luceneQuery;
		}
		else {
			BooleanQuery classFilter = new BooleanQuery();
			//annihilate the scoring impact of DocumentBuilder.CLASS_FIELDNAME
			classFilter.setBoost( 0 );
			for ( Class clazz : classesAndSubclasses ) {
				Term t = new Term( DocumentBuilder.CLASS_FIELDNAME, clazz.getName() );
				TermQuery termQuery = new TermQuery( t );
				classFilter.add( termQuery, BooleanClause.Occur.SHOULD );
			}
			BooleanQuery filteredQuery = new BooleanQuery();
			filteredQuery.add( luceneQuery, BooleanClause.Occur.MUST );
			filteredQuery.add( classFilter, BooleanClause.Occur.MUST );
			return filteredQuery;
		}
	}

	private int max(int first, Hits hits) {
		return maxResults == null ?
				hits.length() - 1 :
				maxResults + first < hits.length() ?
						first + maxResults - 1 :
						hits.length() - 1;
	}

	private int first() {
		return firstResult != null ?
				firstResult :
				0;
	}

	//TODO change classesAndSubclasses by side effect, which is a mismatch with the Searcher return, fix that.
	private Searcher buildSearcher(FullTextIndexEventListener listener) {
		Map<Class, DocumentBuilder<Object>> builders = listener.getDocumentBuilders();
		Set<Directory> directories = new HashSet<Directory>();
		if ( classes == null || classes.length == 0 ) {
			//no class means all classes
			for ( DocumentBuilder builder : builders.values() ) {
				directories.add( builder.getDirectoryProvider().getDirectory() );
			}
			classesAndSubclasses = null;
		}
		else {
			Set<Class> involvedClasses = new HashSet<Class>( classes.length );
			Collections.addAll( involvedClasses, classes );
			for ( Class clazz : classes ) {
				DocumentBuilder builder = builders.get( clazz );
				if ( builder != null ) involvedClasses.addAll( builder.getMappedSubclasses() );
			}
			for ( Class clazz : involvedClasses ) {
				DocumentBuilder builder = builders.get( clazz );
				//TODO should we rather choose a polymorphic path and allow non mapped entities
				if ( builder == null ) throw new HibernateException( "Not a mapped entity: " + clazz );
				directories.add( builder.getDirectoryProvider().getDirectory() );
			}
			classesAndSubclasses = involvedClasses;
		}

		//set up the searcher
		Searcher searcher;
		int dirNbr = directories.size();
		if ( dirNbr > 1 ) {
			try {
				IndexSearcher[] searchers = new IndexSearcher[dirNbr];
				Iterator<Directory> it = directories.iterator();
				for ( int index = 0; index < dirNbr; index++ ) {
					searchers[index] = new IndexSearcher( it.next() );
				}
				searcher = new MultiSearcher( searchers );
			}
			catch (IOException e) {
				throw new HibernateException( "Unable to read Lucene directory", e );
			}
		}
		else {
			try {
				searcher = new IndexSearcher( directories.iterator().next() );
			}
			catch (IOException e) {
				throw new HibernateException( "Unable to read Lucene directory", e );
			}
		}
		return searcher;
	}

	private void setResultSize(Hits hits) {
		resultSize = hits.length();
	}

	//FIXME does it make sense
	public int resultSize() {
		return this.resultSize;
	}

	public Query setFirstResult(int firstResult) {
		this.firstResult = firstResult;
		return this;
	}

	public Query setMaxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	public int executeUpdate() throws HibernateException {
		throw new HibernateException( "Not supported operation" );
	}

	public Query setLockMode(String alias, LockMode lockMode) {
		return null;
	}

	protected Map getLockModes() {
		return null;
	}
}
