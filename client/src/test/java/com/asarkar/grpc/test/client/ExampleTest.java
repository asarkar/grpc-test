package com.asarkar.grpc.test.client;

import com.asarkar.grpc.test.GrpcCleanupExtension;
import com.asarkar.grpc.test.Resources;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@ExtendWith(GrpcCleanupExtension.class)
class ExampleTest {
  private final Server server = Mockito.mock(Server.class);
  private final ManagedChannel channel1 = Mockito.mock(ManagedChannel.class);
  private final ManagedChannel channel2 = Mockito.mock(ManagedChannel.class);

  //  @AfterEach
  //  void afterEach() {
  //    System.out.println(mockingDetails(server).printInvocations());
  //    System.out.println(mockingDetails(channel1).printInvocations());
  //    System.out.println(mockingDetails(channel2).printInvocations());
  //  }

  @Test
  void testSuccessful(Resources resources) throws InterruptedException {
    Mockito.when(server.awaitTermination(
            ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
        .thenReturn(true);
    Mockito.when(channel1.awaitTermination(
            ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
        .thenReturn(true);
    Mockito.when(channel2.awaitTermination(
            ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
        .thenReturn(true);
    resources.register(server); // use default timeout
    resources.register(channel1, Duration.ofSeconds(1)); // override default timeout
    resources.timeout(Duration.ofSeconds(3)); // change default timeout to 3 seconds
    resources.register(
        channel2); // channel2 timeout is 3 seconds; server and channel timeouts didn't change
  }
}
