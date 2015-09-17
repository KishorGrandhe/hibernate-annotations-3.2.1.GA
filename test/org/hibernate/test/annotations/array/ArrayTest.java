//$Id: ArrayTest.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.array;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.test.annotations.TestCase;

/**
 * @author Emmanuel Bernard
 */
public class ArrayTest extends TestCase {

	public void testOneToMany() throws Exception {
		Session s;
		Transaction tx;
		s = openSession();
		tx = s.beginTransaction();
		Competitor c1 = new Competitor();
		c1.setName( "Renault" );
		Competitor c2 = new Competitor();
		c2.setName( "Ferrari" );
		Contest contest = new Contest();
		contest.setResults( new Competitor[]{c1, c2} );
		s.persist( contest );
		tx.commit();
		s.close();

		s = openSession();
		tx = s.beginTransaction();
		contest = (Contest) s.get( Contest.class, contest.getId() );
		assertNotNull( contest );
		assertNotNull( contest.getResults() );
		assertEquals( 2, contest.getResults().length );
		assertEquals( c2.getName(), contest.getResults()[1].getName() );
		tx.commit();
		s.close();
	}

	public ArrayTest(String x) {
		super( x );
	}

	protected Class[] getMappings() {
		return new Class[]{
				Competitor.class,
				Contest.class
		};
	}
}
