package org.fuin.porelo.agent.impl.omp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fuin.porelo.agent.impl.omp.WorkflowSteps.Run;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs the build workflow's publishing steps against a stub {@code curl}, with the repository's own
 * {@code artifactory.sh} beside them, so what they upload and what they believe is seen, not looked
 * for.
 */
class PublishStepsTest {

    private static final String DEB = "porelo-agent-omp_0.5.0_amd64.deb";

    private static final String OLDER =
            "Package: porelo-agent-omp\nVersion: 0.4.1\n"
                    + "Filename: pool/main/p/porelo-agent-omp/porelo-agent-omp_0.4.1_amd64.deb\n\n";

    private static final String THIS =
            "Package: porelo-agent-omp\nVersion: 0.5.0\n"
                    + "Filename: pool/main/p/porelo-agent-omp/"
                    + DEB
                    + "\n\n";

    @Test
    void aReleaseIsNotUploadedWhenTheIndexCannotBeRead(@TempDir final Path dir) throws Exception {

        // A failed fetch read as "not published" replaces a release people already installed.
        for (final String step : publish()) {
            final Map<String, String> env = stubs(dir, OLDER);
            env.put("CURL_EXIT", "22");
            final Run run = WorkflowSteps.run(step, dir, env, WorkflowSteps.LIMIT);
            assertThat(run.exit())
                    .as("exit with the index unreadable:%n%s", run.output())
                    .isNotZero();
            assertThat(calls(dir))
                    .as("uploads with the index unreadable")
                    .noneMatch(PublishStepsTest::upload);
        }
    }

    @Test
    void aPublishedReleaseIsNotReplaced(@TempDir final Path dir) throws Exception {

        for (final String step : publish()) {
            final Run run =
                    WorkflowSteps.run(step, dir, stubs(dir, OLDER + THIS), WorkflowSteps.LIMIT);
            assertThat(run.exit())
                    .as("exit with the release published:%n%s", run.output())
                    .isNotZero();
            assertThat(run.output()).contains("this release is published already");
            assertThat(calls(dir))
                    .as("uploads with the release published")
                    .noneMatch(PublishStepsTest::upload);
        }
    }

    @Test
    void aReleaseNotYetPublishedIsUploaded(@TempDir final Path dir) throws Exception {

        // The refusals above are the index's, not a step that never uploads.
        for (final String step : publish()) {
            final Run run = WorkflowSteps.run(step, dir, stubs(dir, OLDER), WorkflowSteps.LIMIT);
            assertThat(run.exit())
                    .as("exit with an older release published:%n%s", run.output())
                    .isZero();
            assertThat(calls(dir))
                    .as("uploads with an older release published")
                    .anyMatch(PublishStepsTest::upload);
        }
    }

    @Test
    void aPackageIsIndexedOnlyWithItsOwnVersion(@TempDir final Path dir) throws Exception {

        // An index listing only an older version would pass a publish that never arrived.
        final List<String> steps =
                WorkflowSteps.scripts("build.yml", "The package is indexed, not merely stored");
        assertThat(steps).as("the index steps of the build and the release job").hasSize(2);
        for (final String step : steps) {
            final Run older = WorkflowSteps.run(step, dir, stubs(dir, OLDER), WorkflowSteps.LIMIT);
            assertThat(older.exit())
                    .as("exit with only an older version indexed:%n%s", older.output())
                    .isEqualTo(1);

            final Run indexed =
                    WorkflowSteps.run(step, dir, stubs(dir, OLDER + THIS), WorkflowSteps.LIMIT);
            assertThat(indexed.exit())
                    .as("exit with this version indexed:%n%s", indexed.output())
                    .isZero();
        }
    }

    @Test
    void aBranchNameIsNeverRunAsShell(@TempDir final Path dir) throws Exception {

        // Anyone who can push names a branch; a name interpolated into the script runs as shell
        // with the job's token. The runner replaces each expression before bash starts, as here.
        final String ref = "refs/heads/x'$(touch ran)'";
        final List<String> steps = WorkflowSteps.scripts("build.yml", "Not publishing");
        assertThat(steps).as("the step that says why nothing is published").hasSize(1);
        final String script =
                steps.get(0)
                        .replace("${{ github.event_name }}", "push")
                        .replace("${{ github.ref }}", ref);
        final Run run =
                WorkflowSteps.run(
                        script, dir, Map.of("EVENT", "push", "REF", ref), WorkflowSteps.LIMIT);
        assertThat(run.exit()).as("exit of the step:%n%s", run.output()).isZero();
        assertThat(dir.resolve("ran")).as("a file the branch's name created").doesNotExist();
        assertThat(run.output()).as("the branch named as it is").contains(ref);
    }

    private static List<String> publish() throws IOException {
        final List<String> steps = WorkflowSteps.scripts("build.yml", "Publish the packages");
        assertThat(steps).as("the publishing steps of the build and the release job").hasSize(2);
        return steps;
    }

    /** An upload is a {@code curl} that sends a file, as {@code art_put} does. */
    private static boolean upload(final String call) {
        return call.startsWith("curl ") && call.contains(" -T ");
    }

    private static List<String> calls(final Path dir) throws IOException {
        final Path calls = dir.resolve("calls");
        return Files.exists(calls) ? Files.readAllLines(calls) : List.of();
    }

    /**
     * Writes the built files, an index holding the given stanzas, the repository's {@code
     * artifactory.sh} where the step sources it, and stubs for curl and sleep on the PATH; returns
     * the environment that points the step at them.
     */
    private static Map<String, String> stubs(final Path dir, final String index)
            throws IOException {
        final Path bin = Files.createDirectories(dir.resolve("bin"));
        final Path target = Files.createDirectories(dir.resolve("target"));
        Files.writeString(target.resolve(DEB), "deb");
        Files.writeString(target.resolve("porelo-agent-omp-0.5.0-1.x86_64.rpm"), "rpm");
        Files.writeString(target.resolve("porelo-agent-omp.cdx.json"), "{}");
        Files.writeString(dir.resolve("index"), index);
        Files.deleteIfExists(dir.resolve("calls"));
        final Path scripts = Files.createDirectories(dir.resolve(".github/scripts"));
        Files.copy(
                Path.of(".github/scripts/artifactory.sh"),
                scripts.resolve("artifactory.sh"),
                StandardCopyOption.REPLACE_EXISTING);
        script(
                bin.resolve("curl"),
                """
                echo "curl $*" >> "$CALLS"
                [ "${CURL_EXIT:-0}" = 0 ] || exit "$CURL_EXIT"
                out=
                while [ $# -gt 0 ]; do
                    case "$1" in -o) out="$2"; shift ;; esac
                    shift
                done
                if [ -n "$out" ]; then cat "$INDEX" > "$out"; else cat "$INDEX"; fi
                """);
        // The index step waits between tries; here nothing is gained by waiting.
        script(bin.resolve("sleep"), "exit 0\n");
        final Map<String, String> env = new HashMap<>();
        env.put("PATH", bin + ":" + System.getenv("PATH"));
        env.put("CALLS", dir.resolve("calls").toString());
        env.put("INDEX", dir.resolve("index").toString());
        env.put("JF_URL", "https://artifactory.invalid");
        env.put("JF_ACCESS_TOKEN", "not-a-token");
        env.put("CHANNEL", "releases");
        return env;
    }

    private static void script(final Path file, final String body) throws IOException {
        Files.writeString(file, "#!/bin/sh\n" + body);
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwxr-xr-x"));
    }
}
