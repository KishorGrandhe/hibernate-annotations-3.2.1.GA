//$Id: File.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.inheritance.joined;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

/**
 * @author Emmanuel Bernard
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class File {
	@Id
	private String name;
	@ManyToOne
	private Folder parent;

	File() {
	}

	public File(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}

	public void setName(String id) {
		this.name = id;
	}

	public Folder getParent() {
		return parent;
	}

	public void setParent(Folder parent) {
		this.parent = parent;
	}

}
