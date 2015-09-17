//$Id: Contest.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.array;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.IndexColumn;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Contest {
	private int id;
	private Competitor[] results;

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@OneToMany(cascade = CascadeType.ALL)
	@IndexColumn(name = "pos")
	public Competitor[] getResults() {
		return results;
	}

	public void setResults(Competitor[] results) {
		this.results = results;
	}
}
