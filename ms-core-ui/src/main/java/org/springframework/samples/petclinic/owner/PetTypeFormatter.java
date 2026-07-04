package org.springframework.samples.petclinic.owner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;

@Component
public class PetTypeFormatter implements Formatter<PetType> {

	private final RestTemplate restTemplate;

	private final String vetsOwnersUrl;

	public PetTypeFormatter(RestTemplate restTemplate, @Value("${services.vets-owners.url}") String vetsOwnersUrl) {
		this.restTemplate = restTemplate;
		this.vetsOwnersUrl = vetsOwnersUrl;
	}

	@Override
	public String print(PetType petType, Locale locale) {
		String name = petType.getName();
		return name != null ? name : "<null>";
	}

	@Override
	public PetType parse(String text, Locale locale) throws ParseException {
		PetType[] findPetTypes = this.restTemplate.getForObject(vetsOwnersUrl + "/api/petTypes", PetType[].class);
		if (findPetTypes != null) {
			for (PetType type : findPetTypes) {
				if (Objects.equals(type.getName(), text)) {
					return type;
				}
			}
		}
		throw new ParseException("type not found: " + text, 0);
	}

}
