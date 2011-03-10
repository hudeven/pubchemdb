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
import org.apache.commons.pipeline.stage.ExtendedBaseStage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/*
 * @author Mark Southern (southern at scripps dot edu)
 */
public abstract class SessionStage extends ExtendedBaseStage {

	private Map<Thread, Session> sessions = new ConcurrentHashMap<Thread, Session>();
	private Map<Thread, Transaction> transactions = new ConcurrentHashMap<Thread, Transaction>();

	public abstract SessionFactory getSessionFactory();

	public Session getSession() {
		Session session = sessions.get(Thread.currentThread());
		if (session == null) {
			session = getSessionFactory().openSession();
			setSession(session);
			setTransaction(session.beginTransaction());
		}
		return session;
	}

	public void setSession(Session session) {
		sessions.put(Thread.currentThread(), session);
	}

	public Transaction getTransaction() {
		return transactions.get(Thread.currentThread());
	}

	public void setTransaction(Transaction transaction) {
		transactions.put(Thread.currentThread(), transaction);
	}

	@Override
	public void innerPostprocess() throws StageException {
		try {
			for (Transaction tx : transactions.values())
				if (!tx.wasCommitted())
					tx.commit();
			for (Session session : sessions.values()) {
				session.close();
			}
			sessions = null;
			transactions = null;
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	public String status() {
		return "";
	}
}
