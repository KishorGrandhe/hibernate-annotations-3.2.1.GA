//$Id: $
package org.hibernate.test.reflection.java.generics.deep;

import junit.framework.TestCase;
import org.hibernate.reflection.java.JavaXFactory;
import org.hibernate.reflection.XClass;


/**
 * @author Emmanuel Bernard
 */
public class DeepGenericsInheritance extends TestCase {
	public void test2StepsGenerics() throws Exception {
		JavaXFactory factory = new JavaXFactory();
		XClass subclass2 = factory.toXClass( Subclass2.class );
		XClass dummySubclass = factory.toXClass( DummySubclass.class );
		XClass superclass = subclass2.getSuperclass();
		XClass supersuperclass = superclass.getSuperclass();
		assertTrue( supersuperclass.getDeclaredProperties( "field" ).get( 1 ).isTypeResolved() );
		assertEquals( dummySubclass, supersuperclass.getDeclaredProperties( "field" ).get( 1 ).getType() );

	}
}
