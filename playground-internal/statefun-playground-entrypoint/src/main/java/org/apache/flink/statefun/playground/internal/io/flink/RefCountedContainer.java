package org.apache.flink.statefun.playground.internal.io.flink;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.apache.flink.util.Preconditions;

final class RefCountedContainer<T extends AutoCloseable> {
  private final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private T value;

  @GuardedBy("lock")
  private int refCounter;

  RefCountedContainer() {
    this.value = null;
    this.refCounter = 0;
  }

  Lease getOrCreate(Supplier<T> supplier) {
    synchronized (lock) {
      if (value == null) {
        value = supplier.get();
      }

      return new Lease();
    }
  }

  class Lease implements AutoCloseable {

    private boolean valid;

    private Lease() {
      valid = true;
      synchronized (lock) {
        refCounter += 1;
      }
    }

    @Override
    public void close() throws Exception {
      if (valid) {
        synchronized (lock) {
          refCounter -= 1;

          if (refCounter == 0 && value != null) {
            value.close();
            value = null;
          }
        }

        valid = false;
      }
    }

    T deref() {
      Preconditions.checkState(valid, "Lease is no longer valid");
      synchronized (lock) {
        return Preconditions.checkNotNull(
            value, "Value is null while there are still valid leases. This should not happen.");
      }
    }
  }
}
