package ilep.codesubmission.assignment;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request and response bodies for the instructor assignment API. */
public final class AssignmentRequests {

	private AssignmentRequests() {
	}

	/**
	 * @param assignmentMode "assignment" or "exam". Exam rules are enforced from Phase 4.
	 */
	public record CreateAssignmentRequest(
			@NotBlank @Size(max = 200) String title,
			@Size(max = 20_000) String instructions,
			@Size(max = 65_536) String starterCode,
			@Pattern(regexp = "^(assignment|exam)$") String assignmentMode,
			OffsetDateTime opensAt,
			OffsetDateTime dueAt,
			@NotNull @Valid AssignmentSettings settings) {
	}

	/**
	 * @param artificialIntelligenceUse forbidden, restricted or permitted (AI Policy §VI.B.1)
	 */
	public record AssignmentSettings(
			@NotNull @Pattern(regexp = "^(forbidden|restricted|permitted)$") String artificialIntelligenceUse,
			@Size(max = 2_000) String artificialIntelligenceNote) {
	}

	public record CreateAssignmentResponse(UUID assignmentId, String joinCode, String joinUrl) {
	}

	public record StudentSummary(
			String studentReference,
			String status,
			OffsetDateTime startedAt,
			OffsetDateTime lastSavedAt,
			OffsetDateTime submittedAt,
			int runCount) {
	}
}
