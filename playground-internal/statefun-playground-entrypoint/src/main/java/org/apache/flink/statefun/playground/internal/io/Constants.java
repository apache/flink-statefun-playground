package org.apache.flink.statefun.playground.internal.io;

import com.google.protobuf.Message;
import org.apache.flink.statefun.sdk.EgressType;
import org.apache.flink.statefun.sdk.IngressType;
import org.apache.flink.statefun.sdk.io.EgressIdentifier;
import org.apache.flink.statefun.sdk.io.IngressIdentifier;
import org.apache.flink.statefun.sdk.reqreply.generated.TypedValue;

public class Constants {
  private static final String NAMESPACE = "io.statefun.playground";
  private static final String EGRESS = "egress";
  private static final String INGRESS = "ingress";

  public static final EgressType EGRESS_TYPE = new EgressType(NAMESPACE, EGRESS);
  public static final IngressType INGRESS_TYPE = new IngressType(NAMESPACE, INGRESS);
  public static final EgressIdentifier<TypedValue> EGRESS_IDENTIFIER =
      new EgressIdentifier<>(NAMESPACE, EGRESS, TypedValue.class);
  public static final IngressIdentifier<Message> INGRESS_IDENTIFIER =
      new IngressIdentifier<>(Message.class, NAMESPACE, INGRESS);

  public static final String DEFAULT_INGRESS_TYPE = "io.statefun.types/string";
  public static final String STATEFUN_CONTENT_TYPE_PREFIX = "application/vnd.";
  public static final String PLAYGROUND_EGRESS_RECORD = "io.statefun.playground/EgressRecord";

  private Constants() {
    throw new UnsupportedOperationException("Should not be instantiated.");
  }
}
