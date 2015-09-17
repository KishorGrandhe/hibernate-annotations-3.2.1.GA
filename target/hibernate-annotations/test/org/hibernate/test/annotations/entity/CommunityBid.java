//$Id: CommunityBid.java 8974 2006-01-04 00:43:49Z epbernard $
package org.hibernate.test.annotations.entity;

import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class CommunityBid extends Bid {
	private Starred communityNote;

	public Starred getCommunityNote() {
		return communityNote;
	}

	public void setCommunityNote(Starred communityNote) {
		this.communityNote = communityNote;
	}

}
