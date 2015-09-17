//$Id: $
package org.hibernate.test.annotations.tableperclass;

import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Entity;
import javax.persistence.InheritanceType;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Length;
import org.hibernate.annotations.Index;

/**
 * @author Emmanuel Bernard
 */
@Entity(name = "xpmComponent")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Component {
	private String manufacturerPartNumber;
	private Long manufacturerId;
	private Long id;



	public void setId(Long id) {
		this.id = id;
	}


	@Id
	public Long getId() {
		return id;
	}


	@NotNull
	@Length(max = 40)
	@Index(name = "manufacturerPartNumber")
	public String getManufacturerPartNumber() {
		return manufacturerPartNumber;
	}

	@NotNull
	public Long getManufacturerId() {
		return manufacturerId;
	}

	public void setManufacturerId(Long manufacturerId) {
		this.manufacturerId = manufacturerId;
	}


	public void setManufacturerPartNumber(String manufacturerPartNumber) {
		this.manufacturerPartNumber = manufacturerPartNumber;
	}
}