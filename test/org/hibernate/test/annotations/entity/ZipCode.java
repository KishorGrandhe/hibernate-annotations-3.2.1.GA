//$Id: ZipCode.java 8974 2006-01-04 00:43:49Z epbernard $
package org.hibernate.test.annotations.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author Emmanuel Bernard
 */
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Entity
@org.hibernate.annotations.Entity(mutable = false)
public class ZipCode {
	@Id
	public String code;
}
