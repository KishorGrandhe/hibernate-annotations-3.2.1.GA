//$Id: $
package org.hibernate.search.test;

import java.io.File;
import java.util.List;

import org.hibernate.Session;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * @author Emmanuel Bernard
 */
public class RamDirectoryTest extends TestCase {

	public void testMultipleEntitiesPerIndex() throws Exception {


		Session s = getSessions().openSession();
		s.getTransaction().begin();
		Document document =
				new Document( "Hibernate in Action", "Object/relational mapping with Hibernate", "blah blah blah" );
		s.persist(document);
		s.flush();
		s.persist(
				new AlternateDocument( document.getId(), "Hibernate in Action", "Object/relational mapping with Hibernate", "blah blah blah" )
		);
		s.getTransaction().commit();
		s.close();

		assertEquals( 2, getDocumentNbr() );

		s = getSessions().openSession();
		s.getTransaction().begin();
		s.delete( s.get( AlternateDocument.class, document.getId() ) );
		s.getTransaction().commit();
		s.close();

		assertEquals( 1, getDocumentNbr() );

		s = getSessions().openSession();
		s.getTransaction().begin();
		s.delete( s.createCriteria( Document.class ).uniqueResult() );
		s.getTransaction().commit();
		s.close();
	}

	private int getDocumentNbr() throws Exception {
		IndexReader reader = IndexReader.open( getDirectory( Document.class ) );
		try {
			return reader.numDocs();
		}
		finally {
			reader.close();
		}
	}

	protected Class[] getMappings() {
		return new Class[]{
				Document.class,
				AlternateDocument.class
		};
	}

}
