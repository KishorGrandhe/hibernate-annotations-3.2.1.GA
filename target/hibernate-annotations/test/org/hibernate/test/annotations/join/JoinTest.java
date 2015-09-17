//$Id: JoinTest.java 9833 2006-04-30 04:08:48Z epbernard $
package org.hibernate.test.annotations.join;

import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.mapping.Join;
import org.hibernate.test.annotations.TestCase;

/**
 * @author Emmanuel Bernard
 */
public class JoinTest extends TestCase {

	public JoinTest(String x) {
		super( x );
	}

	public void testDefaultValue() throws Exception {
		Join join = (Join) getCfg().getClassMapping( Life.class.getName() ).getJoinClosureIterator().next();
		assertEquals( "ExtendedLife", join.getTable().getName() );
		org.hibernate.mapping.Column owner = new org.hibernate.mapping.Column();
		owner.setName( "LIFE_ID" );
		assertTrue( join.getTable().getPrimaryKey().containsColumn( owner ) );
		Session s = openSession();
		Transaction tx = s.beginTransaction();
		Life life = new Life();
		life.duration = 15;
		life.fullDescription = "Long long description";
		s.persist( life );
		tx.commit();
		s.close();

		s = openSession();
		tx = s.beginTransaction();
		Query q = s.createQuery( "from " + Life.class.getName() );
		life = (Life) q.uniqueResult();
		assertEquals( "Long long description", life.fullDescription );
		tx.commit();
		s.close();
	}

	public void testCompositePK() throws Exception {
		Join join = (Join) getCfg().getClassMapping( Dog.class.getName() ).getJoinClosureIterator().next();
		assertEquals( "DogThoroughbred", join.getTable().getName() );
		org.hibernate.mapping.Column owner = new org.hibernate.mapping.Column();
		owner.setName( "OWNER_NAME" );
		assertTrue( join.getTable().getPrimaryKey().containsColumn( owner ) );
		Session s = openSession();
		Transaction tx = s.beginTransaction();
		Dog dog = new Dog();
		DogPk id = new DogPk();
		id.name = "Thalie";
		id.ownerName = "Martine";
		dog.id = id;
		dog.weight = 30;
		dog.thoroughbredName = "Colley";
		s.persist( dog );
		tx.commit();
		s.close();

		s = openSession();
		tx = s.beginTransaction();
		Query q = s.createQuery( "from Dog" );
		dog = (Dog) q.uniqueResult();
		assertEquals( "Colley", dog.thoroughbredName );
		tx.commit();
		s.close();
	}

	public void testExplicitValue() throws Exception {
		Session s = openSession();
		Transaction tx = s.beginTransaction();
		Death death = new Death();
		death.date = new Date();
		death.howDoesItHappen = "Well, haven't seen it";
		s.persist( death );
		tx.commit();
		s.close();

		s = openSession();
		tx = s.beginTransaction();
		Query q = s.createQuery( "from " + Death.class.getName() );
		death = (Death) q.uniqueResult();
		assertEquals( "Well, haven't seen it", death.howDoesItHappen );
		s.delete( death );
		tx.commit();
		s.close();
	}

	public void testManyToOne() throws Exception {
		Session s = openSession();
		Transaction tx = s.beginTransaction();
		Life life = new Life();
		Cat cat = new Cat();
		cat.setName( "kitty" );
		life.duration = 15;
		life.fullDescription = "Long long description";
		life.owner = cat;
		s.persist( life );
		tx.commit();
		s.close();

		s = openSession();
		tx = s.beginTransaction();
		Criteria crit = s.createCriteria( Life.class );
		crit.createCriteria( "owner" ).add( Expression.eq( "name", "kitty" ) );
		life = (Life) crit.uniqueResult();
		assertEquals( "Long long description", life.fullDescription );
		s.delete( life.owner );
		s.delete( life );
		tx.commit();
		s.close();
	}

	public void testUniqueConstaintOnSecondaryTable() throws Exception {
		Cat cat = new Cat();
		cat.setStoryPart2( "My long story" );
		Cat cat2 = new Cat();
		cat2.setStoryPart2( "My long story" );
		Session s = openSession();
		Transaction tx = s.beginTransaction();
		try {
			s.persist( cat );
			s.persist( cat2 );
			tx.commit();
			fail( "unique constraints violation on secondary table" );
		}
		catch (HibernateException e) {
			//success
		}
		finally {
			if ( tx != null ) tx.rollback();
			s.close();
		}
	}

	/**
	 * @see org.hibernate.test.annotations.TestCase#getMappings()
	 */
	protected Class[] getMappings() {
		return new Class[]{
				Life.class,
				Death.class,
				Cat.class,
				Dog.class
		};
	}

}
