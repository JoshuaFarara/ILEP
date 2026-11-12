# Sandbox image for running one student Java program.
# Build:  docker build -t ilep-java-runner:25 code-submission-service/runner-image
#
# The service starts each run with strict limits (see DockerJavaCodeRunner):
# no network, read-only root file system, memory/CPU/process limits, all capabilities dropped,
# non-root user, and a wall-clock timeout.
FROM eclipse-temurin:25-jdk

COPY run-submission.sh /usr/local/bin/run-submission.sh
RUN chmod 0755 /usr/local/bin/run-submission.sh

# 65534 is the conventional "nobody" user: no home directory, no privileges.
USER 65534:65534
WORKDIR /workspace
ENTRYPOINT ["/usr/local/bin/run-submission.sh"]
