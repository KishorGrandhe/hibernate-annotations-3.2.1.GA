//$Id: Document.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.inheritance.joined;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Document extends File {
	@Column(nullable = false)
	private int size;

	Document() {
	}

	Document(String name, int size) {
		super( name );
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
