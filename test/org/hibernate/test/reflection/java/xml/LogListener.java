//$Id: $
package org.hibernate.test.reflection.java.xml;

import javax.persistence.PrePersist;
import javax.persistence.PostPersist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Emmanuel Bernard
 */
public class LogListener {
	Log log = LogFactory.getLog( LogListener.class );

	@PrePersist
	@PostPersist
	public void log(Object entity) {
		log.debug( "Logging entity " +  entity.getClass().getName() + " with hashCode: " + entity.hashCode() );
	}


	public void noLog(Object entity) {
		log.debug( "NoLogging entity " +  entity.getClass().getName() + " with hashCode: " + entity.hashCode() );
	}
}
