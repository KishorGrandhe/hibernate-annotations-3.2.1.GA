//$Id: VegetablePk.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.inheritance;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 * @author Emmanuel Bernard
 */
@Embeddable
public class VegetablePk implements Serializable {
	private String farmer;

	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( !( o instanceof VegetablePk ) ) return false;

		final VegetablePk vegetablePk = (VegetablePk) o;

		if ( !farmer.equals( vegetablePk.farmer ) ) return false;
		if ( !harvestDate.equals( vegetablePk.harvestDate ) ) return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = farmer.hashCode();
		result = 29 * result + harvestDate.hashCode();
		return result;
	}

	public String getFarmer() {
		return farmer;
	}

	public void setFarmer(String farmer) {
		this.farmer = farmer;
	}

	public String getHarvestDate() {
		return harvestDate;
	}

	public void setHarvestDate(String harvestDate) {
		this.harvestDate = harvestDate;
	}

	private String harvestDate;
}
