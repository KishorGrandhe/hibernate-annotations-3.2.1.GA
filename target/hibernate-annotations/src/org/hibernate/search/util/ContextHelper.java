//$Id: $
package org.hibernate.search.util;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.search.event.FullTextIndexEventListener;

/**
 * @author Emmanuel Bernard
 */
public abstract class ContextHelper {

	public static FullTextIndexEventListener getLuceneEventListener(SessionImplementor session) {
		PostInsertEventListener[] listeners = session.getListeners().getPostInsertEventListeners();
		FullTextIndexEventListener listener = null;
		//FIXME this sucks since we mandante the event listener use
		for ( PostInsertEventListener candidate : listeners ) {
			if ( candidate instanceof FullTextIndexEventListener ) {
				listener = (FullTextIndexEventListener) candidate;
				break;
			}
		}
		if ( listener == null ) throw new HibernateException( "Lucene event listener not initialized" );
		return listener;
	}
}
