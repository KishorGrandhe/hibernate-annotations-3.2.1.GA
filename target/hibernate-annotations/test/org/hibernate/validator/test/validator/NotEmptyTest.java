//$Id: $
package org.hibernate.validator.test.validator;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Date;
import java.io.Serializable;
import java.math.BigInteger;

import junit.framework.TestCase;
import org.hibernate.validator.test.Address;
import org.hibernate.validator.test.Brother;
import org.hibernate.validator.test.Tv;
import org.hibernate.validator.test.Vase;
import org.hibernate.validator.test.Site;
import org.hibernate.validator.test.Contact;
import org.hibernate.validator.test.ValidatorTest;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.MessageInterpolator;
import org.hibernate.validator.Validator;

/**
 * @author Gavin King
 */
public class NotEmptyTest extends TestCase {

	public void testBigInteger() throws Exception {
		Car car = new Car();
		ClassValidator<Car> classValidator = new ClassValidator<Car>( Car.class );
		InvalidValue[] invalidValues = classValidator.getInvalidValues( car );
		assertEquals( 2, invalidValues.length );
		car.name = "";
		invalidValues = classValidator.getInvalidValues( car );
		assertEquals( 2, invalidValues.length );
		car.name = "350Z";
		invalidValues = classValidator.getInvalidValues( car );
		assertEquals( 1, invalidValues.length );
		car.insurances = new String[0];
		invalidValues = classValidator.getInvalidValues( car );
		assertEquals( 1, invalidValues.length );
		car.insurances = new String[1];
		invalidValues = classValidator.getInvalidValues( car );
		assertEquals( 0, invalidValues.length );
	}
}
