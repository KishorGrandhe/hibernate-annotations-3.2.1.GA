package org.hibernate.test.reflection.java;

import com.sun.org.apache.xpath.internal.operations.Number;
import junit.framework.TestCase;
import org.hibernate.reflection.ReflectionManager;
import org.hibernate.reflection.XClass;
import org.hibernate.reflection.java.JavaXFactory;

/**
 * @author Paolo Perrotta
 */
public class JavaReflectionManagerTest extends TestCase {

	private ReflectionManager rm = new JavaXFactory();

	public void testReturnsAnXClassThatWrapsTheGivenClass() {
		XClass xc = rm.toXClass( Integer.class );
		assertEquals( "java.lang.Integer", xc.getName() );
	}

	public void testReturnsSameXClassForSameClass() {
		XClass xc1 = rm.toXClass( void.class );
		XClass xc2 = rm.toXClass( void.class );
		assertSame( xc2, xc1 );
	}

	public void testReturnsNullForANullClass() {
		assertNull( rm.toXClass( null ) );
	}

	public void testComparesXClassesWithClasses() {
		XClass xc = rm.toXClass( Integer.class );
		assertTrue( rm.equals( xc, Integer.class ) );
	}

	public void testSupportsNullsInComparisons() {
		XClass xc = rm.toXClass( Integer.class );
		assertFalse( rm.equals( null, Number.class ) );
		assertFalse( rm.equals( xc, null ) );
		assertTrue( rm.equals( null, null ) );
	}
}
