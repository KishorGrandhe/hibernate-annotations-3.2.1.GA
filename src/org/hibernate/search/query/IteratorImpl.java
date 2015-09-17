//$Id: $
package org.hibernate.search.query;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

/**
 * @author Emmanuel Bernard
 */
//TODO load the next batch-size elements to benefit from batch-size 
public class IteratorImpl implements Iterator {

	private final List<EntityInfo> entityInfos;
	private final Session session;
	private int index = 0;
	private final int size;

	public IteratorImpl(List<EntityInfo> entityInfos, Session session) {
		this.entityInfos = entityInfos;
		this.session = session;
		this.size = entityInfos.size();
	}

	public boolean hasNext() {
		return index < size;
	}

	public Object next() {
		Object object = session.get( entityInfos.get( index ).clazz, entityInfos.get( index ).id );
		index++;
		return object;
	}

	public void remove() {
		//TODO this is theorically doable
		throw new UnsupportedOperationException( "Cannot remove from a lucene query iterator" );
	}
}
