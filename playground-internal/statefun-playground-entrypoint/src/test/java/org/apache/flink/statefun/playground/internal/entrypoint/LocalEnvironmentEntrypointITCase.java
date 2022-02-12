package org.apache.flink.statefun.playground.internal.entrypoint;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class LocalEnvironmentEntrypointITCase {

  @Disabled("Test for manual verification")
  @Test
  public void testLocalEnvironmentEntrypoint() throws Exception {
    LocalEnvironmentEntrypoint.main(new String[] {"--module", "classpath:module.yaml"});
  }
}
