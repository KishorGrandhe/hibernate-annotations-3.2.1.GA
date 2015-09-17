//$Id: $
package org.hibernate.search.backend;

import java.io.Serializable;

/**
 * @author Emmanuel Bernard
 */
public class DeleteWork extends Work {
	public DeleteWork(Serializable id, Class entity) {
		super( id, entity );
	}
}
