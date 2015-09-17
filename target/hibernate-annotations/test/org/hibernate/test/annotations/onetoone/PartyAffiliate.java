//$Id: PartyAffiliate.java 8974 2006-01-04 00:43:49Z epbernard $
package org.hibernate.test.annotations.onetoone;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class PartyAffiliate {
	@Id
	String partyId;

	@OneToOne
	@PrimaryKeyJoinColumn
	Party party;

	String affiliateName;
}
