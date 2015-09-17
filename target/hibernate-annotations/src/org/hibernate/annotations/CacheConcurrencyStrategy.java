//$Id: CacheConcurrencyStrategy.java 9795 2006-04-26 06:41:18Z epbernard $
package org.hibernate.annotations;

/**
 * Cache concurrency strategy
 *
 * @author Emmanuel Bernard
 */
public enum CacheConcurrencyStrategy {
	NONE,
	READ_ONLY,
	NONSTRICT_READ_WRITE,
	READ_WRITE,
	TRANSACTIONAL
}
