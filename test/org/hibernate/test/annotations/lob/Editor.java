//$Id: Editor.java 7336 2005-06-27 17:02:11Z epbernard $
package org.hibernate.test.annotations.lob;

import java.io.Serializable;

/**
 * @author Emmanuel Bernard
 */
public class Editor implements Serializable {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
