//$Id: $
package org.hibernate.search.backend;

import java.io.Serializable;

import org.apache.lucene.document.Document;

/**
 * Represent a Lucene unit work
 *
 * @author Emmanuel Bernard
 */
public abstract class Work implements Serializable {
	private Document document;
	private Class entity;
	private Serializable id;

	public Work(Serializable id, Class entity) {
		this( id, entity, null );
	}

	public Work(Serializable id, Class entity, Document document) {
		this.id = id;
		this.entity = entity;
		this.document = document;
	}


	public Document getDocument() {
		return document;
	}

	public Class getEntity() {
		return entity;
	}

	public Serializable getId() {
		return id;
	}
}
