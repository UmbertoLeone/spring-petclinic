package org.springframework.samples.petclinic.model;

import jakarta.validation.constraints.NotBlank;

public class NamedEntity extends BaseEntity {

	@NotBlank
	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		String name = this.getName();
		return name != null ? name : "<null>";
	}

}
