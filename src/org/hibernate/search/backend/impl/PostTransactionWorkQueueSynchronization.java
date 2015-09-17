//$Id: $
package org.hibernate.search.backend.impl;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.hibernate.search.backend.WorkQueue;
import org.hibernate.search.backend.Work;
import org.hibernate.search.util.WeakIdentityHashMap;

/**
 * Execute some work inside a transaction sychronization
 *
 * @author Emmanuel Bernard
 */
public class PostTransactionWorkQueueSynchronization implements Synchronization {
	private WorkQueue workQueue;
	private boolean consumed;
	private WeakIdentityHashMap queuePerTransaction;

	public PostTransactionWorkQueueSynchronization(WorkQueue workQueue) {
		this.workQueue = workQueue;
	}

	public PostTransactionWorkQueueSynchronization(WorkQueue workQueue, WeakIdentityHashMap queuePerTransaction) {
		this(workQueue);
		this.queuePerTransaction = queuePerTransaction;
	}

	public void add(Work work) {
		workQueue.add( work );
	}

	public boolean isConsumed() {
		return consumed;
	}

	public void beforeCompletion() {
	}

	public void afterCompletion(int i) {
		try {
			if ( Status.STATUS_COMMITTED == i ) {
				workQueue.performWork();
			}
			else {
				workQueue.cancelWork();
			}
		}
		finally {
			consumed = true;
			//clean the Synchronization per Transaction
			//not needed stricto sensus but a cleaner approach and faster than the GC
			if (queuePerTransaction != null) queuePerTransaction.removeValue( this ); 
		}
	}
}
