package org.springframework.samples.petclinic.owner;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class OwnerRestController {

	private final OwnerRepository ownerRepository;

	private final PetTypeRepository petTypeRepository;

	public OwnerRestController(OwnerRepository ownerRepository, PetTypeRepository petTypeRepository) {
		this.ownerRepository = ownerRepository;
		this.petTypeRepository = petTypeRepository;
	}

	@GetMapping("/owners")
	public Page<Owner> getOwners(@RequestParam(value = "lastName", required = false, defaultValue = "") String lastName,
			@RequestParam(value = "page", defaultValue = "1") int page) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return ownerRepository.findByLastNameStartingWith(lastName, pageable);
	}

	@GetMapping("/owners/{ownerId}")
	public ResponseEntity<Owner> getOwner(@PathVariable int ownerId) {
		return ownerRepository.findById(ownerId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/owners")
	@ResponseStatus(HttpStatus.CREATED)
	public Owner createOwner(@Valid @RequestBody Owner owner) {
		return ownerRepository.save(owner);
	}

	@PutMapping("/owners/{ownerId}")
	public ResponseEntity<Owner> updateOwner(@PathVariable int ownerId, @Valid @RequestBody Owner owner) {
		if (!ownerRepository.existsById(ownerId)) {
			return ResponseEntity.notFound().build();
		}
		owner.setId(ownerId);
		return ResponseEntity.ok(ownerRepository.save(owner));
	}

	@GetMapping("/petTypes")
	public Collection<PetType> getPetTypes() {
		return petTypeRepository.findPetTypes();
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}")
	public ResponseEntity<Pet> getPet(@PathVariable int ownerId, @PathVariable int petId) {
		Optional<Owner> ownerOpt = ownerRepository.findById(ownerId);
		if (ownerOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Pet pet = ownerOpt.get().getPet(petId);
		if (pet == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(pet);
	}

	@PostMapping("/owners/{ownerId}/pets")
	public ResponseEntity<Pet> addPet(@PathVariable int ownerId, @Valid @RequestBody Pet pet) {
		Optional<Owner> ownerOpt = ownerRepository.findById(ownerId);
		if (ownerOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Owner owner = ownerOpt.get();
		owner.addPet(pet);
		ownerRepository.save(owner);
		Pet savedPet = owner.getPets().stream().filter(p -> p.getName().equals(pet.getName())).findFirst().orElse(pet);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedPet);
	}

	@PutMapping("/owners/{ownerId}/pets/{petId}")
	public ResponseEntity<Pet> updatePet(@PathVariable int ownerId, @PathVariable int petId,
			@Valid @RequestBody Pet pet) {
		Optional<Owner> ownerOpt = ownerRepository.findById(ownerId);
		if (ownerOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Owner owner = ownerOpt.get();
		Pet existingPet = owner.getPet(petId);
		if (existingPet == null) {
			return ResponseEntity.notFound().build();
		}
		existingPet.setName(pet.getName());
		existingPet.setBirthDate(pet.getBirthDate());
		existingPet.setType(pet.getType());
		ownerRepository.save(owner);
		return ResponseEntity.ok(existingPet);
	}

}
