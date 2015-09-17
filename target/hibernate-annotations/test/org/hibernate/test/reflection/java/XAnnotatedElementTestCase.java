package org.hibernate.test.reflection.java;

import javax.persistence.Transient;

import junit.framework.TestCase;
import org.hibernate.reflection.XAnnotatedElement;
import org.hibernate.test.reflection.java.generics.TestAnnotation;

/**
 * @author Paolo Perrotta
 */
public abstract class XAnnotatedElementTestCase extends TestCase {

	public void testKnowsWhetherAnAnnotationIsPresent() {
		assertTrue( getConcreteInstance().isAnnotationPresent( TestAnnotation.class ) );
		assertFalse( getConcreteInstance().isAnnotationPresent( Transient.class ) );
	}

	public void testReturnsSpecificAnnotations() {
		TestAnnotation ent = getConcreteInstance().getAnnotation( TestAnnotation.class );
		assertEquals( "xyz", ent.name() );
	}

	protected abstract XAnnotatedElement getConcreteInstance();
}
