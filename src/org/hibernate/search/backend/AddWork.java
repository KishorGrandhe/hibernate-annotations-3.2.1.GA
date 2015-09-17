//$Id: $
package org.hibernate.search.backend;

import java.io.Serializable;

import org.apache.lucene.document.Document;

/**
 * @author Emmanuel Bernard
 */
public class AddWork extends Work {
	public AddWork(Serializable id, Class entity, Document document) {
		super( id, entity, document );
	}
}
