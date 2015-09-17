//$Id: InheritanceTest.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.validator.test.inheritance;

import junit.framework.TestCase;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

/**
 * @author Emmanuel Bernard
 */
public class InheritanceTest extends TestCase {
	public void testInheritance() throws Exception {
		ClassValidator<Dog> classValidator = new ClassValidator<Dog>( Dog.class );
		Dog dog = new Dog();
		InvalidValue[] invalidValues = classValidator.getInvalidValues( dog );
		assertEquals( 3, invalidValues.length );
		dog.setFavoriteBone( "DE" ); //failure
		invalidValues = classValidator.getInvalidValues( dog );
		assertEquals( 3, invalidValues.length );
	}
}
