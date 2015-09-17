//$Id: $
package org.hibernate.search.backend.impl;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.search.engine.DocumentBuilder;
import org.hibernate.search.backend.Workspace;
import org.hibernate.search.backend.DeleteWork;
import org.hibernate.search.backend.AddWork;
import org.hibernate.search.backend.UpdateWork;
import org.hibernate.search.backend.Work;

/**
 * Stateless implementation that perform a work
 *
 * @author Emmanuel Bernard
 */
public class LuceneWorker {
	private Workspace workspace;
	private static Log log = LogFactory.getLog( LuceneWorker.class );

	public LuceneWorker(Workspace workspace) {
		this.workspace = workspace;
	}

	public void performWork(Work work) {
		if ( AddWork.class.isAssignableFrom( work.getClass() ) ) {
			performWork( (AddWork) work );
		}
		else if ( UpdateWork.class.isAssignableFrom( work.getClass() ) ) {
			performWork( (UpdateWork) work );
		}
		else if ( DeleteWork.class.isAssignableFrom( work.getClass() ) ) {
			performWork( (DeleteWork) work );
		}
		else {
			throw new AssertionFailure( "Unknown work type: " + work.getClass() );
		}
	}

	public void performWork(AddWork work) {
		Class entity = work.getEntity();
		Serializable id = work.getId();
		Document document = work.getDocument();
		add( entity, id, document );
	}

	private void add(Class entity, Serializable id, Document document) {
		if ( log.isTraceEnabled() )
			log.trace( "add to Lucene index: " + entity + "#" + id + ": " + document );
		IndexWriter writer = workspace.getIndexWriter( entity );
		try {
			writer.addDocument( document );
		}
		catch (IOException e) {
			throw new HibernateException( "Unable to add to Lucene index: " + entity + "#" + id, e );
		}
	}

	public void performWork(UpdateWork work) {
		Class entity = work.getEntity();
		Serializable id = work.getId();
		Document document = work.getDocument();
		remove( entity, id );
		add( entity, id, document );
	}

	public void performWork(DeleteWork work) {
		Class entity = work.getEntity();
		Serializable id = work.getId();
		remove( entity, id );
	}

	private void remove(Class entity, Serializable id) {
		log.trace( "remove from Lucene index: " + entity + "#" + id );
		DocumentBuilder builder = workspace.getDocumentBuilder( entity );
		Term term = builder.getTerm( id );
		IndexReader reader = workspace.getIndexReader( entity );
		TermDocs termDocs = null;
		try {
			//TODO is there a faster way?
			//TODO include TermDocs into the workspace?
			termDocs = reader.termDocs( term );
			String entityName = entity.getName();
			while ( termDocs.next() ) {
				int docIndex = termDocs.doc();
				if ( entityName.equals( reader.document( docIndex ).get( DocumentBuilder.CLASS_FIELDNAME ) ) ) {
					//remove only the one of the right class
					//loop all to remove all the matches (defensive code)
					reader.deleteDocument( docIndex );
				}
			}
		}
		catch (Exception e) {
			throw new HibernateException( "Unable to remove from Lucene index: " + entity + "#" + id, e );
		}
		finally {
			if (termDocs != null) try {
				termDocs.close();
			}
			catch (IOException e) {
				log.warn( "Unable to close termDocs properly", e);
			}
		}
	}
}
