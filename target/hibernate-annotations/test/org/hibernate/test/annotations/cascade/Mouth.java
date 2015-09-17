//$Id: Mouth.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.cascade;

import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Mouth {
	@Id
	@GeneratedValue
	public Integer id;
	public int size;
	@OneToMany(mappedBy = "mouth", cascade = CascadeType.REMOVE)
	public Collection<Tooth> teeth;
}
