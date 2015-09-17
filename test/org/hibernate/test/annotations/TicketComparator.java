//$Id: TicketComparator.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.test.annotations;

import java.util.Comparator;

/**
 * Ticket comparator ordering longest first
 *
 * @author Emmanuel Bernard
 */
public class TicketComparator implements Comparator<Ticket> {

	public int compare(Ticket ticket, Ticket ticket1) {
		if ( ticket == null || ticket1 == null ) {
			throw new IllegalStateException( "Ticket comparison only available through non null tickets" );
		}
		return ticket1.getNumber().length() - ticket.getNumber().length();

	}
}
