//$Id: CollectionElementTest.java 10809 2006-11-15 04:41:36Z epbernard $
package org.hibernate.test.annotations.collectionelement;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.test.annotations.Country;
import org.hibernate.test.annotations.TestCase;

/**
 * @author Emmanuel Bernard
 */
public class CollectionElementTest extends TestCase {

	public void testSimpleElement() throws Exception {
		assertEquals(
				"BoyFavoriteNumbers",
				getCfg().getCollectionMapping( Boy.class.getName() + '.' + "favoriteNumbers" )
						.getCollectionTable().getName()
		);
		Session s = openSession();
		s.getTransaction().begin();
		Boy boy = new Boy();
		boy.setFirstName( "John" );
		boy.setLastName( "Doe" );
		boy.getNickNames().add( "Johnny" );
		boy.getNickNames().add( "Thing" );
		boy.getScorePerNickName().put( "Johnny", new Integer( 3 ) );
		boy.getScorePerNickName().put( "Thing", new Integer( 5 ) );
		int[] favNbrs = new int[4];
		for ( int index = 0; index < favNbrs.length - 1; index++ ) {
			favNbrs[index] = index * 3;
		}
		boy.setFavoriteNumbers( favNbrs );
		boy.getCharacters().add( Character.GENTLE );
		boy.getCharacters().add( Character.CRAFTY );
		s.persist( boy );
		s.getTransaction().commit();
		s.clear();
		Transaction tx = s.beginTransaction();
		boy = (Boy) s.get( Boy.class, boy.getId() );
		assertNotNull( boy.getNickNames() );
		assertTrue( boy.getNickNames().contains( "Thing" ) );
		assertNotNull( boy.getScorePerNickName() );
		assertTrue( boy.getScorePerNickName().containsKey( "Thing" ) );
		assertEquals( new Integer( 5 ), boy.getScorePerNickName().get( "Thing" ) );
		assertNotNull( boy.getFavoriteNumbers() );
		assertEquals( 3, boy.getFavoriteNumbers()[1] );
		assertTrue( boy.getCharacters().contains( Character.CRAFTY ) );
		List result = s.createQuery( "select boy from Boy boy join boy.nickNames names where names = :name" )
				.setParameter( "name", "Thing" ).list();
		assertEquals( 1, result.size() );
		s.delete( boy );
		tx.commit();
		s.close();
	}

	public void testCompositeElement() throws Exception {
		Session s = openSession();
		s.getTransaction().begin();
		Boy boy = new Boy();
		boy.setFirstName( "John" );
		boy.setLastName( "Doe" );
		Toy toy = new Toy();
		toy.setName( "Balloon" );
		toy.setSerial( "serial001" );
		toy.setBrand( new Brand() );
		toy.getBrand().setName( "Bandai" );
		boy.getFavoriteToys().add( toy );
		s.persist( boy );
		s.getTransaction().commit();
		s.clear();
		Transaction tx = s.beginTransaction();
		boy = (Boy) s.get( Boy.class, boy.getId() );
		assertNotNull( boy.getFavoriteToys() );
		assertTrue( boy.getFavoriteToys().contains( toy ) );
		assertEquals( "@Parent is failing", boy, boy.getFavoriteToys().iterator().next().getOwner() );
		s.delete( boy );
		tx.commit();
		s.close();
	}

	public void testAttributedJoin() throws Exception {
		Session s = openSession();
		s.getTransaction().begin();
		Country country = new Country();
		country.setName( "Australia" );
		s.persist( country );

		Boy boy = new Boy();
		boy.setFirstName( "John" );
		boy.setLastName( "Doe" );
		CountryAttitude attitude = new CountryAttitude();
		// TODO: doesn't work
		attitude.setBoy( boy );
		attitude.setCountry( country );
		attitude.setLikes( true );
		boy.getCountryAttitudes().add( attitude );
		s.persist( boy );
		s.getTransaction().commit();
		s.clear();

		Transaction tx = s.beginTransaction();
		boy = (Boy) s.get( Boy.class, boy.getId() );
		assertTrue( boy.getCountryAttitudes().contains( attitude ) );
		s.delete( boy );
		s.delete( s.get( Country.class, country.getId() ) );
		tx.commit();
		s.close();

	}

	public void testLazyCollectionofElements() throws Exception {
		assertEquals(
				"BoyFavoriteNumbers",
				getCfg().getCollectionMapping( Boy.class.getName() + '.' + "favoriteNumbers" )
						.getCollectionTable().getName()
		);
		Session s = openSession();
		s.getTransaction().begin();
		Boy boy = new Boy();
		boy.setFirstName( "John" );
		boy.setLastName( "Doe" );
		boy.getNickNames().add( "Johnny" );
		boy.getNickNames().add( "Thing" );
		boy.getScorePerNickName().put( "Johnny", new Integer( 3 ) );
		boy.getScorePerNickName().put( "Thing", new Integer( 5 ) );
		int[] favNbrs = new int[4];
		for ( int index = 0; index < favNbrs.length - 1; index++ ) {
			favNbrs[index] = index * 3;
		}
		boy.setFavoriteNumbers( favNbrs );
		boy.getCharacters().add( Character.GENTLE );
		boy.getCharacters().add( Character.CRAFTY );
		s.persist( boy );
		s.getTransaction().commit();
		s.clear();
		Transaction tx = s.beginTransaction();
		boy = (Boy) s.get( Boy.class, boy.getId() );
		assertNotNull( boy.getNickNames() );
		assertTrue( boy.getNickNames().contains( "Thing" ) );
		assertNotNull( boy.getScorePerNickName() );
		assertTrue( boy.getScorePerNickName().containsKey( "Thing" ) );
		assertEquals( new Integer( 5 ), boy.getScorePerNickName().get( "Thing" ) );
		assertNotNull( boy.getFavoriteNumbers() );
		assertEquals( 3, boy.getFavoriteNumbers()[1] );
		assertTrue( boy.getCharacters().contains( Character.CRAFTY ) );
		List result = s.createQuery( "select boy from Boy boy join boy.nickNames names where names = :name" )
				.setParameter( "name", "Thing" ).list();
		assertEquals( 1, result.size() );
		s.delete( boy );
		tx.commit();
		s.close();
	}


	protected Class[] getMappings() {
		return new Class[]{
				Boy.class,
				Country.class
		};
	}
}
