/**
 * 
 */
package org.hibernate.test.annotationfactory;

/**
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 */
@interface TestAnnotation {
	String stringElement();

	String elementWithDefault() default "abc";

	boolean booleanElement();

	String someOtherElement();
}