package io.lenses.streamreactor.connect.testcontainers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.FrameConsumerResultCallback;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.ToStringConsumer;

import java.nio.charset.Charset;

class RootContainerExec {


    private static final Charset OUTPUT_CHARSET = Charset.defaultCharset();

    public static ContainerExecResult installPackage(GenericContainer container, String pkg) throws InterruptedException {
        return rootExecInContainer(container, String.format("microdnf install %s", pkg));
    }

    private static ContainerExecResult rootExecInContainer(
            GenericContainer<?> container,
            String command
    ) throws InterruptedException {

        ToStringConsumer stdoutConsumer = new ToStringConsumer();
        ToStringConsumer stderrConsumer = new ToStringConsumer();
        FrameConsumerResultCallback callback = createCallback(stdoutConsumer, stderrConsumer);

        DockerClient dockerClient = DockerClientFactory.instance().client();
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient
                .execCreateCmd(container.getContainerId())
                .withUser("root")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(command)
                .exec();
        dockerClient.execStartCmd(execCreateCmdResponse.getId()).exec(callback).awaitCompletion();
        Long exitCode = dockerClient.inspectExecCmd(execCreateCmdResponse.getId()).exec().getExitCodeLong();

        return new ContainerExecResult(exitCode, stdoutConsumer.toString(OUTPUT_CHARSET), stderrConsumer.toString(OUTPUT_CHARSET));

    }

    private static FrameConsumerResultCallback createCallback(ToStringConsumer stdoutConsumer, ToStringConsumer stderrConsumer) {
        FrameConsumerResultCallback callback = new FrameConsumerResultCallback();
        callback.addConsumer(OutputFrame.OutputType.STDOUT, stdoutConsumer);
        callback.addConsumer(OutputFrame.OutputType.STDERR, stderrConsumer);
        return callback;
    }

    public static class ContainerExecResult {

        final long exitCode;

        final String stdout;

        final String stderr;

        public ContainerExecResult(long exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }

}
