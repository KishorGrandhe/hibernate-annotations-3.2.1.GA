//$Id: A320.java 9044 2006-01-13 01:58:41Z epbernard $
package org.hibernate.test.annotations;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@DiscriminatorValue("A320")
@Entity()
public class A320 extends Plane {
	private String javaEmbeddedVersion;

	public String getJavaEmbeddedVersion() {
		return javaEmbeddedVersion;
	}

	public void setJavaEmbeddedVersion(String string) {
		javaEmbeddedVersion = string;
	}

}
