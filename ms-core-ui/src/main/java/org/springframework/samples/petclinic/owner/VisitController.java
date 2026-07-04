package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
class VisitController {

	private final RestTemplate restTemplate;

	private final String vetsOwnersUrl;

	private final String visitsUrl;

	public VisitController(RestTemplate restTemplate, @Value("${services.vets-owners.url}") String vetsOwnersUrl,
			@Value("${services.visits.url}") String visitsUrl) {
		this.restTemplate = restTemplate;
		this.vetsOwnersUrl = vetsOwnersUrl;
		this.visitsUrl = visitsUrl;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			Map<String, Object> model) {
		Owner owner = restTemplate.getForObject(vetsOwnersUrl + "/api/owners/" + ownerId, Owner.class);
		if (owner == null) {
			throw new IllegalArgumentException(
					"Owner not found with id: " + ownerId + ". Please ensure the ID is correct ");
		}

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}

		Visit[] visits = restTemplate.getForObject(visitsUrl + "/api/pets/" + petId + "/visits", Visit[].class);
		if (visits != null) {
			pet.getVisits().clear();
			pet.getVisits().addAll(Arrays.asList(visits));
		}

		model.put("pet", pet);
		model.put("owner", owner);

		Visit visit = new Visit();
		pet.addVisit(visit);
		return visit;
	}

	@ModelAttribute("minVisitDate")
	public LocalDate minVisitDate() {
		return LocalDate.now().plusDays(1);
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String initNewVisitForm() {
		return "pets/createOrUpdateVisitForm";
	}

	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@ModelAttribute Owner owner, @PathVariable int petId, @Valid Visit visit,
			BindingResult result, RedirectAttributes redirectAttributes) {
		if (visit.getDate() != null && !visit.getDate().isAfter(LocalDate.now())) {
			result.rejectValue("date", "typeMismatch.visitDate");
		}

		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		}

		restTemplate.postForObject(visitsUrl + "/api/pets/" + petId + "/visits", visit, Visit.class);
		redirectAttributes.addFlashAttribute("message", "Your visit has been booked");
		return "redirect:/owners/{ownerId}";
	}

}
