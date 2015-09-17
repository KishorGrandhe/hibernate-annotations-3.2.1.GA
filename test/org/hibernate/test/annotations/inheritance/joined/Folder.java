//$Id: Folder.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.inheritance.joined;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Folder extends File {
	@OneToMany(mappedBy = "parent")
	private Set<File> children = new HashSet<File>();

	Folder() {
	}

	public Folder(String name) {
		super( name );
	}

	public Set<File> getChildren() {
		return children;
	}

	public void setChildren(Set children) {
		this.children = children;
	}
}
