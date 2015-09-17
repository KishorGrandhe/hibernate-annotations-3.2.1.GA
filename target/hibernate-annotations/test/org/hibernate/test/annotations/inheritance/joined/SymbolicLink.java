//$Id: SymbolicLink.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.inheritance.joined;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class SymbolicLink extends File {

	@ManyToOne(optional = false)
	File target;

	SymbolicLink() {
	}

	public SymbolicLink(File target) {
		this.target = target;
	}

	public File getTarget() {
		return target;
	}

	public void setTarget(File target) {
		this.target = target;
	}


}
