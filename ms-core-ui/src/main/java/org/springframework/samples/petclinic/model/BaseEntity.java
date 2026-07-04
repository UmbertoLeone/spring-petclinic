package org.springframework.samples.petclinic.model;

import java.io.Serializable;

public class BaseEntity implements Serializable {

	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isNew() {
		return this.id == null;
	}

}
