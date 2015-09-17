//$Id: $
package org.hibernate.search.test.query;

import java.util.List;
import java.util.Iterator;

import org.hibernate.search.test.TestCase;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.Transaction;
import org.hibernate.Hibernate;
import org.hibernate.ScrollableResults;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.analysis.StopAnalyzer;


/**
 * @author Emmanuel Bernard
 */
public class LuceneQueryTest extends TestCase {

	public void testList() throws Exception {
		FullTextSession s = Search.createFullTextSession( openSession() );
		Transaction tx = s.beginTransaction();
		Clock clock = new Clock(1, "Seiko");
		s.save( clock );
		clock = new Clock( 2, "Festina");
		s.save( clock );
		Book book = new Book(1, "La chute de la petite reine a travers les yeux de Festina", "La chute de la petite reine a travers les yeux de Festina, blahblah");
		s.save(book);
		book = new Book(2, "La gloire de mon père", "Les deboires de mon père en vélo");
		s.save(book);
		tx.commit();
		s.clear();
		tx = s.beginTransaction();
		QueryParser parser = new QueryParser("title", new StopAnalyzer() );

		Query query = parser.parse( "summary:noword" );
		org.hibernate.Query hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		List result = hibQuery.list();
		assertNotNull( result );
		assertEquals( 0, result.size() );

		query = parser.parse( "summary:Festina Or brand:Seiko" );
		hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		result = hibQuery.list();
		assertNotNull( result );
		assertEquals( "Query with explicit class filter", 2, result.size() );

        query = parser.parse( "summary:Festina Or brand:Seiko" );
		hibQuery = s.createFullTextQuery( query );
		result = hibQuery.list();
		assertNotNull( result );
		assertEquals( "Query with no class filter", 2, result.size() );
        for (Object element : result) {
			assertTrue( Hibernate.isInitialized( element ) );
			s.delete( element );
		}
		for (Object element : s.createQuery( "from java.lang.Object" ).list() ) s.delete( element );
		tx.commit();
		s.close();
	}

	public void testFirstMax() throws Exception {
		FullTextSession s = Search.createFullTextSession( openSession() );
		Transaction tx = s.beginTransaction();
		Clock clock = new Clock(1, "Seiko");
		s.save( clock );
		clock = new Clock( 2, "Festina");
		s.save( clock );
		Book book = new Book(1, "La chute de la petite reine a travers les yeux de Festina", "La chute de la petite reine a travers les yeux de Festina, blahblah");
		s.save(book);
		book = new Book(2, "La gloire de mon père", "Les deboires de mon père en vélo");
		s.save(book);
		tx.commit();
		s.clear();
		tx = s.beginTransaction();
		QueryParser parser = new QueryParser("title", new StopAnalyzer() );

		Query query = parser.parse( "summary:Festina Or brand:Seiko" );
		org.hibernate.Query hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		hibQuery.setFirstResult( 1 );
		List result = hibQuery.list();
		assertNotNull( result );
		assertEquals( "first result no max result", 1, result.size() );

		hibQuery.setFirstResult( 0 );
		hibQuery.setMaxResults( 1 );
		result = hibQuery.list();
		assertNotNull( result );
		assertEquals( "max result set", 1, result.size() );

		hibQuery.setFirstResult( 0 );
		hibQuery.setMaxResults( 3 );
		result = hibQuery.list();
		assertNotNull( result );
		assertEquals( "max result out of limit", 2, result.size() );

		hibQuery.setFirstResult( 2 );
		hibQuery.setMaxResults( 3 );
		result = hibQuery.list();
		assertNotNull( result );
		assertEquals( "first result out of limit", 0, result.size() );
		
		for (Object element : s.createQuery( "from java.lang.Object" ).list() ) s.delete( element );
		tx.commit();
		s.close();
	}

	public void testIterator() throws Exception {
		FullTextSession s = Search.createFullTextSession( openSession() );
		Transaction tx = s.beginTransaction();
		Clock clock = new Clock(1, "Seiko");
		s.save( clock );
		clock = new Clock( 2, "Festina");
		s.save( clock );
		Book book = new Book(1, "La chute de la petite reine a travers les yeux de Festina", "La chute de la petite reine a travers les yeux de Festina, blahblah");
		s.save(book);
		book = new Book(2, "La gloire de mon père", "Les deboires de mon père en vélo");
		s.save(book);
		tx.commit();//post commit events for lucene
		s.clear();
		tx = s.beginTransaction();
		QueryParser parser = new QueryParser("title", new StopAnalyzer() );

		Query query = parser.parse( "summary:noword" );
		org.hibernate.Query hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		Iterator result = hibQuery.iterate();
		assertNotNull( result );
		assertFalse( result.hasNext() );

		query = parser.parse( "summary:Festina Or brand:Seiko" );
		hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		result = hibQuery.iterate();
		assertNotNull( result );
		int index = 0;
		while ( result.hasNext() ) {
			index++;
			s.delete( result.next() );
		}
		assertEquals( 2, index );
		for (Object element : s.createQuery( "from java.lang.Object" ).list() ) s.delete( element );
		tx.commit();
		s.close();
	}

	public void testScrollableResultSet() throws Exception {
		FullTextSession s = Search.createFullTextSession( openSession() );
		Transaction tx = s.beginTransaction();
		Clock clock = new Clock(1, "Seiko");
		s.save( clock );
		clock = new Clock( 2, "Festina");
		s.save( clock );
		Book book = new Book(1, "La chute de la petite reine a travers les yeux de Festina", "La chute de la petite reine a travers les yeux de Festina, blahblah");
		s.save(book);
		book = new Book(2, "La gloire de mon père", "Les deboires de mon père en vélo");
		s.save(book);
		tx.commit();//post commit events for lucene
		s.clear();
		tx = s.beginTransaction();
		QueryParser parser = new QueryParser("title", new StopAnalyzer() );

		Query query = parser.parse( "summary:noword" );
		org.hibernate.Query hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		ScrollableResults result = hibQuery.scroll();
		assertNotNull( result );
		assertEquals(-1, result.getRowNumber() );
		assertEquals(false, result.next() );
		result.close();

		query = parser.parse( "summary:Festina Or brand:Seiko" );
		hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		result = hibQuery.scroll();
		assertEquals(0, result.getRowNumber() );
		result.beforeFirst();
		assertEquals( true, result.next() );
		assertTrue( result.isFirst() );
		assertTrue( result.scroll( 1 ) );
		assertTrue( result.isLast() );
		assertFalse( result.scroll( 1 ) );
		result.beforeFirst();
		while ( result.next() ) {
			s.delete( result.get()[0] );
		}
		for (Object element : s.createQuery( "from java.lang.Object" ).list() ) s.delete( element );
		tx.commit();
		s.close();
	}

	public void testMultipleEntityPerIndex() throws Exception {
		FullTextSession s = Search.createFullTextSession( openSession() );
		Transaction tx = s.beginTransaction();
		Clock clock = new Clock(1, "Seiko");
		s.save( clock );
		Book book = new Book(1, "La chute de la petite reine a travers les yeux de Festina", "La chute de la petite reine a travers les yeux de Festina, blahblah");
		s.save(book);
		AlternateBook alternateBook = new AlternateBook(1, "La chute de la petite reine a travers les yeux de Festina");
		s.save(alternateBook);
		tx.commit();
		s.clear();
		tx = s.beginTransaction();
		QueryParser parser = new QueryParser("title", new StopAnalyzer() );

		Query query = parser.parse( "summary:Festina" );
		org.hibernate.Query hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		List result = hibQuery.list();
		assertNotNull( result );
		assertEquals( "Query with explicit class filter", 1, result.size() );
		
		query = parser.parse( "summary:Festina" );
		hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		Iterator it = hibQuery.iterate();
		assertTrue( it.hasNext() );
		assertNotNull( it.next() );
		assertFalse( it.hasNext() );

		query = parser.parse( "summary:Festina" );
		hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		ScrollableResults sr = hibQuery.scroll();
		assertTrue( sr.first() );
		assertNotNull( sr.get() );
		assertFalse( sr.next() );
		sr.close();

		query = parser.parse( "summary:Festina OR brand:seiko" );
		hibQuery = s.createFullTextQuery( query, Clock.class, Book.class );
		hibQuery.setMaxResults( 2 );
		result = hibQuery.list();
		assertNotNull( result );
		assertEquals( "Query with explicit class filter and limit", 2, result.size() );

		query = parser.parse( "summary:Festina" );
		hibQuery = s.createFullTextQuery( query );
		result = hibQuery.list();
		assertNotNull( result );
		assertEquals( "Query with no class filter", 2, result.size() );
        for (Object element : result) {
			assertTrue( Hibernate.isInitialized( element ) );
			s.delete( element );
		}
		for (Object element : s.createQuery( "from java.lang.Object" ).list() ) s.delete( element );
		tx.commit();
		s.close();
	}


	protected Class[] getMappings() {
		return new Class[] {
				Book.class,
				AlternateBook.class,
				Clock.class
		};
	}
}
