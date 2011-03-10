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
package edu.scripps.fl.pubchem.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "relation")
@org.hibernate.annotations.Table(appliesTo = "relation", indexes = { @Index(name = "idx_relation_1", columnNames = { "fromId", "fromDb" }) })
public class Relation implements Serializable {

	private String fromDb;
	private Long fromId;
	private Long id;
	private String relationName;
	private String toDb;
	private Long toId;

	@Column(name = "fromDb")
	@Index(name = "idx_relation_fromdb")
	public String getFromDb() {
		return fromDb;
	}

	@Column(name = "fromId")
	public Long getFromId() {
		return fromId;
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	@Index(name = "idx_relation_relname")
	@Column(name = "relation_name")
	public String getRelationName() {
		return relationName;
	}

	@Column(name = "toDb")
	@Index(name = "idx_relation_todb")
	public String getToDb() {
		return toDb;
	}

	@Column(name = "toId")
	public Long getToId() {
		return toId;
	}

	public void setFromDb(String fromDb) {
		this.fromDb = fromDb;
	}

	public void setFromId(Long fromId) {
		this.fromId = fromId;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}

	public void setToDb(String toDb) {
		this.toDb = toDb;
	}

	public void setToId(Long toId) {
		this.toId = toId;
	}

	public String toString() {
		return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this);
	}
}