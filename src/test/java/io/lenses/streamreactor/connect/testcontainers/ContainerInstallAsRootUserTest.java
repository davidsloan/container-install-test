package io.lenses.streamreactor.connect.testcontainers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import static io.lenses.streamreactor.connect.testcontainers.RootContainerExec.ContainerExecResult;

class ContainerInstallAsRootUserTest {

    private static final String CONTAINER_NAME = "confluentinc/cp-kafka";
    private static final String CONTAINER_TAG = "6.1.0";
    private static final Network network = Network.SHARED;

    private static final KafkaContainer kafkaContainer =
            new KafkaContainer(
                    DockerImageName
                            .parse(CONTAINER_NAME)
                            .withTag(CONTAINER_TAG))
                    .withNetwork(network)
                    .withNetworkAliases("kafka");

    @BeforeEach
    public void before() throws Exception {
        kafkaContainer.start();
    }

    @Test
    public void shouldInstallExtraPackages() throws Exception {
        ContainerExecResult execResult = RootContainerExec.installPackage(kafkaContainer, "lzo");
        Assertions.assertEquals(execResult.exitCode, 0);
    }
}