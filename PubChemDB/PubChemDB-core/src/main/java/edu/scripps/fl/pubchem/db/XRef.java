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
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name = "xref", uniqueConstraints = { @UniqueConstraint(columnNames = { "xref_identifier", "xref_database" }) })
public class XRef implements Serializable {

	private Long id = -1L;
	private String database = "";
	private String type = "";
	private String name = "";
	private String xRefId = "";

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof XRef))
			return false;
		XRef other = (XRef) obj;
		if (xRefId == null) {
			if (other.xRefId != null)
				return false;
		} else if (!xRefId.equals(other.xRefId))
			return false;
		return true;
	}

	@Column(name = "xref_database")
	public String getDatabase() {
		return database;
	}

	@Id
	@Column(name = "xref_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	@Column(name = "xref_name", length = 500)
	public String getName() {
		return name;
	}


	@Column(name = "xref_type")
	public String getType() {
		return type;
	}

	@Column(name = "xref_identifier")
	public String getXRefId() {
		return xRefId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((xRefId == null) ? 0 : xRefId.hashCode());
		return result;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}


	public void setType(String type) {
		this.type = type;
	}

	public void setXRefId(String xrefId) {
		this.xRefId = xrefId;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
