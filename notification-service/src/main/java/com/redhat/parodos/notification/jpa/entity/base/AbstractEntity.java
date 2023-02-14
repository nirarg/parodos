/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.notification.jpa.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.util.UUID;

/**
 * @author Richard Wang (Github: RichardW98)
 */
@MappedSuperclass
public class AbstractEntity {

	@Id
	@GeneratedValue
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Version
	@JsonIgnore
	private Long objectVersion;

	protected AbstractEntity() {
		this.id = null;
	}

	void setId(UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return this.id;
	}

	public Long getObjectVersion() {
		return objectVersion;
	}

}
