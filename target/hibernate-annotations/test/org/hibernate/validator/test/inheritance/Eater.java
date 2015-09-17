//$Id: Eater.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.validator.test.inheritance;

import org.hibernate.validator.Min;

/**
 * @author Emmanuel Bernard
 */
public interface Eater {
	@Min(2)
	int getFrequency();

	void setFrequency(int frequency);
}
