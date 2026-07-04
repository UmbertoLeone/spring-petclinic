package org.springframework.samples.petclinic.owner;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class VisitRestController {

	private final VisitRepository visitRepository;

	public VisitRestController(VisitRepository visitRepository) {
		this.visitRepository = visitRepository;
	}

	@GetMapping("/pets/{petId}/visits")
	public List<Visit> getVisitsByPet(@PathVariable int petId) {
		return visitRepository.findByPetId(petId);
	}

	@PostMapping("/pets/{petId}/visits")
	@ResponseStatus(HttpStatus.CREATED)
	public Visit createVisit(@PathVariable int petId, @Valid @RequestBody Visit visit) {
		visit.setPetId(petId);
		return visitRepository.save(visit);
	}

	@GetMapping("/visits")
	public List<Visit> getVisits(@RequestParam(value = "petId", required = false) Integer petId) {
		if (petId != null) {
			return visitRepository.findByPetId(petId);
		}
		return visitRepository.findAll();
	}

}
