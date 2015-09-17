//$Id: Software.java 10881 2006-11-29 15:23:36Z epbernard $
package org.hibernate.test.annotations.indexcoll;

import java.util.Map;
import java.util.HashMap;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Software {
	private String name;
	private Map<String, Version> versions = new HashMap<String, Version>();

	@Id
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(mappedBy = "software")
	@MapKey(name = "codeName")
	public Map<String, Version> getVersions() {
		return versions;
	}

	public void setVersions(Map<String, Version> versions) {
		this.versions = versions;
	}

	public void addVersion(Version version) {
		this.versions.put(version.getCodeName(), version);
	}
}
