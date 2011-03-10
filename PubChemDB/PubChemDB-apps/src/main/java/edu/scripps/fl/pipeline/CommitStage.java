/*
 * Copyright 2010 The Scripps Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.scripps.fl.pipeline;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pipeline.StageException;
import org.hibernate.Session;

/*
 * @author Mark Southern (southern at scripps dot edu)
 */
public abstract class CommitStage extends SessionStage {

	private ThreadBasedCounter counter = new ThreadBasedCounter();

	private int commitFrequency = 10;

	public int getCommitFrequency() {
		return commitFrequency;
	}

	public void setCommitFrequency(int commitFrequency) {
		this.commitFrequency = commitFrequency;
	}

	public void doSave(Object obj) throws Exception {
		getSession().save(obj);
	}

	@Override
	public void innerProcess(Object obj) throws StageException {
		try {
			Session session = getSession();
			int count = counter.incrementAndGet();
			doSave(obj);
			if (count % getCommitFrequency() == 0) {
				getTransaction().commit();
				session.clear();
				setTransaction(session.beginTransaction());
			}
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	class ThreadBasedCounter {

		private Map<Thread, Integer> map = new ConcurrentHashMap<Thread, Integer>();

		public int incrementAndGet() {
			Integer ii = map.get(Thread.currentThread());
			if (ii == null) {
				ii = 0;
			}
			ii++;
			map.put(Thread.currentThread(), ii);
			return ii;
		}
	}
}