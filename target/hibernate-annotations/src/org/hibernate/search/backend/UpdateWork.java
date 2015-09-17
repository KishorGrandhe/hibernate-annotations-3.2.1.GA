//$Id: $
package org.hibernate.search.backend;

import java.io.Serializable;

import org.apache.lucene.document.Document;

/**
 * @author Emmanuel Bernard
 */
public class UpdateWork extends Work {
	public UpdateWork(Serializable id, Class entity, Document document) {
		super( id, entity, document );
	}
}
