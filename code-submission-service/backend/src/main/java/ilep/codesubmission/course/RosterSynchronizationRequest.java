package ilep.codesubmission.course;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A pseudonymous roster sent by ILEP. It carries no names, emails, Eagle IDs or CRNs.
 *
 * @param label         non-identifying course label, e.g. "CSCI 1301 Section A Fall 2026"
 * @param courseSecret  32+ random bytes as hex; the HMAC key ILEP used to compute each joinHash
 * @param students      every student currently enrolled; anyone omitted loses access
 */
public record RosterSynchronizationRequest(
		@NotBlank @Size(max = 200) String label,
		@NotNull @Pattern(regexp = "^([0-9a-fA-F]{2}){32,64}$") String courseSecret,
		@NotNull @Size(max = 1000) List<@Valid RosterStudent> students) {

	/**
	 * @param studentReference opaque ID made by ILEP
	 * @param joinHash         hex HMAC-SHA256(courseSecret, normalized Eagle ID)
	 */
	public record RosterStudent(
			@NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{8,64}$") String studentReference,
			@NotNull @Pattern(regexp = "^[0-9a-fA-F]{64}$") String joinHash) {
	}
}
