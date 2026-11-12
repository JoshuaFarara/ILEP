package ilep.codesubmission.security;

import java.util.UUID;

/** An authenticated student, bound to exactly one work session (one assignment). */
public record StudentPrincipal(UUID studentWorkSessionId) {
}
