//$Id: Stuff.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.generics;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * @author Emmanuel Bernard
 */
@MappedSuperclass
public class Stuff<Value> {
	private Value value;

	@ManyToOne
	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}
}
