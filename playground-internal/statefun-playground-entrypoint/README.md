# Stateful Functions Playground Entrypoint

A simple Stateful Functions entrypoint that runs Stateful Functions within a single process.

## Configuring a module.yaml

Per default the `LocalEnvironmentEntrypoint` expects a `module.yaml` to be on the classpath.
Alternatively, one can provide a different location via `--module file://<PATH>`.

## Configuring the Flink Runtime

One can configure the underlying Flink runtime via `--set <CONFIG_OPTION>=<CONFIG_VALUE>`.