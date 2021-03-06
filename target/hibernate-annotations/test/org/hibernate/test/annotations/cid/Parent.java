//$Id: Parent.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.cid;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * Entity with composite id
 *
 * @author Emmanuel Bernard
 */
@Entity
public class Parent {
	@EmbeddedId
	public ParentPk id;

	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( !( o instanceof Parent ) ) return false;

		final Parent parent = (Parent) o;

		if ( !id.equals( parent.id ) ) return false;

		return true;
	}

	public int hashCode() {
		return id.hashCode();
	}
}
