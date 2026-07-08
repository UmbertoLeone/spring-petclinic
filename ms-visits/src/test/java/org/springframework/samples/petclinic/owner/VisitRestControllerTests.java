package org.springframework.samples.petclinic.owner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VisitRestControllerTests {

	private VisitRepository visitRepository;

	private VisitRestController visitRestController;

	@BeforeEach
	void setUp() {
		this.visitRepository = mock(VisitRepository.class);
		this.visitRestController = new VisitRestController(this.visitRepository);
	}

	@Test
	void shouldGetVisitsByPetId() {
		Visit visit = new Visit();
		visit.setId(1);
		visit.setPetId(7);
		visit.setDate(LocalDate.now());
		visit.setDescription("Regular checkup");

		given(this.visitRepository.findByPetId(7)).willReturn(Collections.singletonList(visit));

		List<Visit> visits = this.visitRestController.getVisitsByPet(7);
		assertEquals(1, visits.size());
		assertEquals("Regular checkup", visits.get(0).getDescription());
	}

	@Test
	void shouldCreateVisit() {
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		visit.setDescription("Vaccination");

		Visit savedVisit = new Visit();
		savedVisit.setId(2);
		savedVisit.setPetId(7);
		savedVisit.setDate(visit.getDate());
		savedVisit.setDescription("Vaccination");

		given(this.visitRepository.save(visit)).willReturn(savedVisit);

		Visit result = this.visitRestController.createVisit(7, visit);
		assertEquals(2, result.getId());
		assertEquals(7, result.getPetId());
		assertEquals("Vaccination", result.getDescription());
	}

}
