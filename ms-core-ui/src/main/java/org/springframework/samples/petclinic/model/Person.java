package org.springframework.samples.petclinic.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Person extends BaseEntity {

	@Size(max = 30)
	@NotBlank
	private String firstName;

	@Size(max = 30)
	@NotBlank
	private String lastName;

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}
