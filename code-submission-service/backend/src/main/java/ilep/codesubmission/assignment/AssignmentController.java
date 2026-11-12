package ilep.codesubmission.assignment;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ilep.codesubmission.assignment.AssignmentRequests.CreateAssignmentRequest;
import ilep.codesubmission.assignment.AssignmentRequests.CreateAssignmentResponse;
import ilep.codesubmission.assignment.AssignmentRequests.StudentSummary;
import ilep.codesubmission.course.CourseController;
import ilep.codesubmission.security.InstructorPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

/** Instructor API: assignments. */
@RestController
@Validated
public class AssignmentController {

	private final AssignmentService assignmentService;

	public AssignmentController(AssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}

	@PostMapping("/api/v1/courses/{courseReference}/assignments")
	@ResponseStatus(HttpStatus.CREATED)
	public CreateAssignmentResponse createAssignment(
			@AuthenticationPrincipal InstructorPrincipal instructor,
			@PathVariable @Pattern(regexp = CourseController.courseReferencePattern) String courseReference,
			@RequestBody @Valid CreateAssignmentRequest request) {
		return assignmentService.createAssignment(instructor.instructorId(), courseReference, request);
	}

	@PostMapping("/api/v1/assignments/{assignmentId}/close")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void closeAssignment(@AuthenticationPrincipal InstructorPrincipal instructor,
			@PathVariable UUID assignmentId) {
		assignmentService.closeAssignment(instructor.instructorId(), assignmentId);
	}

	@GetMapping("/api/v1/assignments/{assignmentId}/summary")
	public List<StudentSummary> summarizeAssignment(@AuthenticationPrincipal InstructorPrincipal instructor,
			@PathVariable UUID assignmentId) {
		return assignmentService.summarizeAssignment(instructor.instructorId(), assignmentId);
	}
}
