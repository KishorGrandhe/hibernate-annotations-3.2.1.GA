//$Id: Party.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations.onetoone;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Party {
	@Id
	String partyId;

	@OneToOne(mappedBy = "party")
	PartyAffiliate partyAffiliate;
}
