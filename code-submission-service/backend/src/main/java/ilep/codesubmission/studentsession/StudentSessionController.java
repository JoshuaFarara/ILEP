package ilep.codesubmission.studentsession;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ilep.codesubmission.coderun.CodeRunResult;
import ilep.codesubmission.coderun.CodeRunService;
import ilep.codesubmission.security.StudentPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Student API: everything a joined student does on their one assignment. */
@RestController
public class StudentSessionController {

	private final StudentWorkSessionService studentWorkSessionService;
	private final CodeRunService codeRunService;

	public StudentSessionController(StudentWorkSessionService studentWorkSessionService, CodeRunService codeRunService) {
		this.studentWorkSessionService = studentWorkSessionService;
		this.codeRunService = codeRunService;
	}

	public record NoticeAcknowledgementRequest(int noticeVersion) {
	}

	public record SaveCodeRequest(@NotNull String code) {
	}

	public record SaveCodeResponse(OffsetDateTime savedAt) {
	}

	public record RunCodeRequest(@NotNull String code, String standardInput) {
	}

	public record SubmitRequest(@NotNull String code) {
	}

	public record SubmitResponse(OffsetDateTime submittedAt) {
	}

	@GetMapping("/api/v1/student/session")
	public StudentWorkSessionService.StudentWorkSessionView viewSession(@AuthenticationPrincipal StudentPrincipal student) {
		return studentWorkSessionService.viewWorkSession(student.studentWorkSessionId());
	}

	@PostMapping("/api/v1/student/session/notice-acknowledgement")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void acknowledgeNotice(@AuthenticationPrincipal StudentPrincipal student,
			@RequestBody NoticeAcknowledgementRequest request) {
		studentWorkSessionService.acknowledgeNotice(student.studentWorkSessionId(), request.noticeVersion());
	}

	@PutMapping("/api/v1/student/session/code")
	public SaveCodeResponse saveCode(@AuthenticationPrincipal StudentPrincipal student,
			@RequestBody @Valid SaveCodeRequest request) {
		return new SaveCodeResponse(studentWorkSessionService.saveCode(student.studentWorkSessionId(), request.code()));
	}

	@PostMapping("/api/v1/student/session/runs")
	public CodeRunResult runCode(@AuthenticationPrincipal StudentPrincipal student,
			@RequestBody @Valid RunCodeRequest request) {
		return codeRunService.runCode(student.studentWorkSessionId(), request.code(), request.standardInput());
	}

	@PostMapping("/api/v1/student/session/submit")
	public SubmitResponse submit(@AuthenticationPrincipal StudentPrincipal student,
			@RequestBody @Valid SubmitRequest request) {
		return new SubmitResponse(studentWorkSessionService.submit(student.studentWorkSessionId(), request.code()));
	}
}
