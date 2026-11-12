package ilep.codesubmission.course;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ilep.codesubmission.security.InstructorPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

/** Instructor API: courses and rosters. */
@RestController
@Validated
public class CourseController {

	public static final String courseReferencePattern = "^[A-Za-z0-9_-]{8,64}$";

	private final RosterSynchronizationService rosterSynchronizationService;

	public CourseController(RosterSynchronizationService rosterSynchronizationService) {
		this.rosterSynchronizationService = rosterSynchronizationService;
	}

	@PutMapping("/api/v1/courses/{courseReference}/roster")
	public RosterSynchronizationService.RosterSynchronizationResult synchronizeRoster(
			@AuthenticationPrincipal InstructorPrincipal instructor,
			@PathVariable @Pattern(regexp = courseReferencePattern) String courseReference,
			@RequestBody @Valid RosterSynchronizationRequest request) {
		return rosterSynchronizationService.synchronizeRoster(instructor.instructorId(), courseReference, request);
	}
}
