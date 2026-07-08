package org.springframework.samples.petclinic.owner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class OwnerRestControllerTests {

	private OwnerRepository ownerRepository;

	private PetTypeRepository petTypeRepository;

	private OwnerRestController ownerRestController;

	@BeforeEach
	void setUp() {
		this.ownerRepository = mock(OwnerRepository.class);
		this.petTypeRepository = mock(PetTypeRepository.class);
		this.ownerRestController = new OwnerRestController(this.ownerRepository, this.petTypeRepository);
	}

	@Test
	void shouldGetOwnerById() {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");

		given(this.ownerRepository.findById(1)).willReturn(Optional.of(owner));

		ResponseEntity<Owner> response = this.ownerRestController.getOwner(1);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("George", response.getBody().getFirstName());
	}

	@Test
	void shouldReturnNotFoundForMissingOwner() {
		given(this.ownerRepository.findById(999)).willReturn(Optional.empty());

		ResponseEntity<Owner> response = this.ownerRestController.getOwner(999);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	void shouldCreateOwner() {
		Owner owner = new Owner();
		owner.setFirstName("George");
		owner.setLastName("Franklin");

		Owner savedOwner = new Owner();
		savedOwner.setId(1);
		savedOwner.setFirstName("George");
		savedOwner.setLastName("Franklin");

		given(this.ownerRepository.save(owner)).willReturn(savedOwner);

		Owner result = this.ownerRestController.createOwner(owner);
		assertEquals(1, result.getId());
		assertEquals("George", result.getFirstName());
	}

}
