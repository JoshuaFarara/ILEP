#!/usr/bin/env bash
# Creates a SYNTHETIC class and assignment through the instructor API, the way ILEP will in Phase 2.
#
#   INSTRUCTOR_API_KEY=... ./scripts/create-synthetic-class.sh [service-url]
#
# Students "Student One" ... "Student Five" have the fake Eagle IDs 900000001 ... 900000005.
# Only pseudonyms and HMAC join hashes are sent; the IDs themselves never leave this script.
# Never put real student data in this script.
set -euo pipefail

service_url="${1:-http://localhost:8080}"
: "${INSTRUCTOR_API_KEY:?Set INSTRUCTOR_API_KEY to the key the service was started with}"

course_reference="synthetic-course-$(openssl rand -hex 4)"
course_secret_hex="$(openssl rand -hex 32)"

join_hash() {
	printf %s "$1" | openssl dgst -sha256 -mac HMAC -macopt "hexkey:${course_secret_hex}" | awk '{print $2}'
}

students_json=""
for student_number in 1 2 3 4 5; do
	student_reference="${course_reference}-student-${student_number}"
	eagle_identifier="90000000${student_number}"
	[[ -n "${students_json}" ]] && students_json+=","
	students_json+="{\"studentReference\":\"${student_reference}\",\"joinHash\":\"$(join_hash "${eagle_identifier}")\"}"
done

curl --fail-with-body --silent --show-error -X PUT "${service_url}/api/v1/courses/${course_reference}/roster" \
	-H "Authorization: Bearer ${INSTRUCTOR_API_KEY}" -H "Content-Type: application/json" \
	-d "{\"label\":\"Synthetic CSCI 1301\",\"courseSecret\":\"${course_secret_hex}\",\"students\":[${students_json}]}" \
	>/dev/null

assignment_json="$(curl --fail-with-body --silent --show-error -X POST \
	"${service_url}/api/v1/courses/${course_reference}/assignments" \
	-H "Authorization: Bearer ${INSTRUCTOR_API_KEY}" -H "Content-Type: application/json" \
	-d @- <<'JSON'
{
  "title": "Lab 4: Counting with Loops",
  "instructions": "Read a whole number n from standard input.\nPrint the numbers 1 to n, one per line.\n\nExample input: 3\nExample output:\n1\n2\n3",
  "starterCode": "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] arguments) {\n        Scanner scanner = new Scanner(System.in);\n        // Write your loop here\n    }\n}\n",
  "settings": {
    "artificialIntelligenceUse": "restricted",
    "artificialIntelligenceNote": "You may ask an approved AI tool to explain a compiler error, but not to write the loop."
  }
}
JSON
)"

echo "Course reference: ${course_reference}"
echo "Assignment:       ${assignment_json}"
echo "Students:         Eagle IDs 900000001 to 900000005 (synthetic)"
