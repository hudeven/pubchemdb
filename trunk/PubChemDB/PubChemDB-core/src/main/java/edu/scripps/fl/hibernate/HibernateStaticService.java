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
package edu.scripps.fl.hibernate;

import java.util.HashMap;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

public class HibernateStaticService {
	private static HashMap<Object, SessionFactory> hibernateSessionFactories = new HashMap<Object, SessionFactory>();

	public static void buildHibernateSessionFactory(AnnotationConfiguration config) {
		setHibernateSessionFactory(null, config.buildSessionFactory());
	}

	public static void buildHibernateSessionFactory(Object key, AnnotationConfiguration config) {
		setHibernateSessionFactory(key, config.buildSessionFactory());
	}

	public static void setHibernateSessionFactory(Object key, SessionFactory sf) {
		hibernateSessionFactories.put(key, sf);
	}

	public static SessionFactory getHibernateSessionFactory() {
		return getHibernateSessionFactory(null);
	}

	public static SessionFactory getHibernateSessionFactory(Object key) {
		return hibernateSessionFactories.get(key);
	}

	public static Session getHibernateSession() {
		return getHibernateSession(null);
	}

	public static Session getHibernateSession(Object key) {
		Session session = getHibernateSessionFactory(key).getCurrentSession();
		if (!session.getTransaction().isActive())
			session.beginTransaction();
		return session;
	}
}