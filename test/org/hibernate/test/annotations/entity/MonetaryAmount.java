//$Id: MonetaryAmount.java 6805 2005-05-16 17:43:09Z epbernard $
package org.hibernate.test.annotations.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * @author Emmanuel Bernard
 */
public class MonetaryAmount implements Serializable {

	private BigDecimal amount;
	private Currency currency;

	public MonetaryAmount(BigDecimal amount, Currency currency) {
		this.amount = amount;
		this.currency = currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}
}
