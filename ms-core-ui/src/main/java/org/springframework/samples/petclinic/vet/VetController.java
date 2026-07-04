package org.springframework.samples.petclinic.vet;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import org.springframework.samples.petclinic.support.CustomPage;

@Controller
class VetController {

	private final RestTemplate restTemplate;

	private final String vetsOwnersUrl;

	public VetController(RestTemplate restTemplate, @Value("${services.vets-owners.url}") String vetsOwnersUrl) {
		this.restTemplate = restTemplate;
		this.vetsOwnersUrl = vetsOwnersUrl;
	}

	@GetMapping("/vets.html")
	public String showVetList(@RequestParam(defaultValue = "1") int page, Model model) {
		String url = vetsOwnersUrl + "/api/vets?page=" + page;
		CustomPage<Vet> paginated = restTemplate
			.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<CustomPage<Vet>>() {
			})
			.getBody();

		return addPaginationModel(page, paginated, model);
	}

	private String addPaginationModel(int page, CustomPage<Vet> paginated, Model model) {
		if (paginated == null) {
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", 0);
			model.addAttribute("totalItems", 0);
			model.addAttribute("listVets", List.of());
			return "vets/vetList";
		}
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		return "vets/vetList";
	}

	@GetMapping({ "/vets" })
	public @ResponseBody Vets showResourcesVetList() {
		return restTemplate.getForObject(vetsOwnersUrl + "/api/vets/all", Vets.class);
	}

}
