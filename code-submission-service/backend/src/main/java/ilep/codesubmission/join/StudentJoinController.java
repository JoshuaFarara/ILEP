package ilep.codesubmission.join;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Student API: join an assignment. Open to anyone; rate-limited by {@link JoinAttemptLimiter}. */
@RestController
public class StudentJoinController {

	private final StudentJoinService studentJoinService;

	public StudentJoinController(StudentJoinService studentJoinService) {
		this.studentJoinService = studentJoinService;
	}

	public record JoinRequest(
			@NotBlank @Size(max = 20) String joinCode,
			@NotBlank @Size(max = 40) String eagleIdentifier) {

		/** Never print the Eagle ID, for example in a debug log of this record. */
		@Override
		public String toString() {
			return "JoinRequest[joinCode=" + joinCode + ", eagleIdentifier=(hidden)]";
		}
	}

	@PostMapping("/api/v1/join")
	public StudentJoinService.JoinResult join(@RequestBody @Valid JoinRequest joinRequest, HttpServletRequest request) {
		return studentJoinService.join(joinRequest.joinCode(), joinRequest.eagleIdentifier(), request.getRemoteAddr());
	}
}
