//$Id: GoalKeeper.java 7861 2005-08-11 22:27:13Z epbernard $
package org.hibernate.test.annotations.id;

import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class GoalKeeper extends Footballer {
	public GoalKeeper() {
	}

	public GoalKeeper(String firstname, String lastname, String club) {
		super( firstname, lastname, club );
	}
}
