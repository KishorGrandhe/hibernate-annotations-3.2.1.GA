//$Id: $
package org.hibernate.search.test.session;

import java.util.List;

import org.hibernate.search.test.TestCase;
import org.hibernate.search.impl.FullTextSessionImpl;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.Transaction;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.analysis.StopAnalyzer;

/**
 * @author Emmanuel Bernard
 */
public class MassIndexTest extends TestCase {

	public void testTransactional() throws Exception {
		FullTextSession s = Search.createFullTextSession( openSession() );
		Transaction tx = s.beginTransaction();
		int loop = 4;
		for (int i = 0 ; i < loop; i++) {
			Email email = new Email();
			email.setTitle( "JBoss World Berlin" );
			email.setBody( "Meet the guys who wrote the software");
			s.persist( email );
		}
		tx.commit();
		s.close();

		s = new FullTextSessionImpl( openSession() );
		s.getTransaction().begin();
		s.connection().createStatement().executeUpdate( "update Email set body='Meet the guys who write the software'");
		s.getTransaction().commit();
		s.close();

		s = new FullTextSessionImpl( openSession() );
		tx = s.beginTransaction();
		QueryParser parser = new QueryParser("id", new StopAnalyzer() );
		List result = s.createFullTextQuery( parser.parse( "body:write" ) ).list();
		assertEquals( 0, result.size() );
		result = s.createCriteria( Email.class ).list();
		for (int i = 0 ; i < loop/2 ; i++)
			s.index( result.get( i ) );
		tx.commit(); //do the process
		s.index( result.get(loop/2) ); //do the process out of tx
		tx = s.beginTransaction();
		for (int i = loop/2+1 ; i < loop; i++)
			s.index( result.get( i ) );
		tx.commit(); //do the process
		s.close();

		s = new FullTextSessionImpl( openSession() );
		tx = s.beginTransaction();
		result = s.createFullTextQuery( parser.parse( "body:write" ) ).list();
		assertEquals( loop, result.size() );
		for (Object o : result) s.delete( o );
		tx.commit();
		s.close();
	}

	protected Class[] getMappings() {
		return new Class[] {
				Email.class
		};
	}
}
