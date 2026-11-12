#!/bin/sh
# Compiles and runs one student program inside the sandbox container.
#
# Input (read-only mount /submission): <ClassName>.java and input.txt (standard input).
# Environment: CLASS_NAME, PROGRAM_TIMEOUT_SECONDS.
# Exit codes: 101 = compile error (errors on standard error), 102 = setup error,
#             124 = time limit reached, otherwise the program's own exit code.
set -u

class_name="${CLASS_NAME:-Main}"
program_timeout_seconds="${PROGRAM_TIMEOUT_SECONDS:-10}"

cp "/submission/${class_name}.java" /workspace/ || exit 102
cd /workspace || exit 102

if ! javac -J-Xmx160m -J-XX:-UsePerfData -encoding UTF-8 -proc:none -d /workspace "${class_name}.java" 1>&2; then
	exit 101
fi

exec timeout --kill-after=2 "${program_timeout_seconds}" \
	java -Xmx128m -Xss1m -XX:-UsePerfData -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xlog:disable -Xlog:all=warning:stderr \
		-Djava.io.tmpdir=/workspace -cp /workspace "${class_name}" < /submission/input.txt
