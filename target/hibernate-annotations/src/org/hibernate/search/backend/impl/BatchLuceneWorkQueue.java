//$Id: $
package org.hibernate.search.backend.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.hibernate.search.engine.DocumentBuilder;
import org.hibernate.search.store.DirectoryProvider;
import org.hibernate.search.backend.impl.LuceneWorker;
import org.hibernate.search.backend.WorkQueue;
import org.hibernate.search.backend.Workspace;
import org.hibernate.search.backend.Work;
import org.hibernate.search.backend.UpdateWork;
import org.hibernate.search.backend.DeleteWork;
import org.hibernate.search.backend.AddWork;

/**
 * @author Emmanuel Bernard
 */
public class BatchLuceneWorkQueue implements WorkQueue {
	private Workspace workspace;
	private LuceneWorker worker;
	private List<Work> queue = new ArrayList<Work>();

	public BatchLuceneWorkQueue(Map<Class, DocumentBuilder<Object>> documentBuilders,
					 Map<DirectoryProvider, ReentrantLock> lockableDirectoryProviders) {
		workspace = new Workspace( documentBuilders, lockableDirectoryProviders );
		worker = new LuceneWorker( workspace );
	}

	public void add(Work work) {
		//TODO optimize by getting rid of dupe works
		if ( work instanceof UpdateWork ) {
			//split in 2 to optimize the process (reader first, writer next
			queue.add( new DeleteWork( work.getId(), work.getEntity() ) );
			queue.add( new AddWork( work.getId(), work.getEntity(), work.getDocument() ) );
		}
		else {
			queue.add( work );
		}
	}

	public void performWork() {
		try {
			//use of index reader
			for ( Work work : queue ) {
				if ( work instanceof DeleteWork ) worker.performWork( work );
			}
			workspace.clean(); //close readers
			for ( Work work : queue ) {
				if ( work instanceof AddWork ) worker.performWork( work );
			}
		}
		finally {
			workspace.clean();
			queue.clear();
		}
	}

	public void cancelWork() {
		queue.clear();
	}
}
