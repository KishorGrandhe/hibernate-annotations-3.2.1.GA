//$Id: Tower.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.id;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
@AttributeOverride(name = "longitude", column = @Column(name = "fld_longitude"))
public class Tower extends MilitaryBuilding {
}
