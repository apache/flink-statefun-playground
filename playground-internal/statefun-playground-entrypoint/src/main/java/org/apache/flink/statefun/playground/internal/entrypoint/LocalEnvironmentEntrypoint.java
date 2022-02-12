/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.statefun.playground.internal.entrypoint;

import java.util.Collection;
import org.apache.flink.api.java.utils.MultipleParameterTool;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.statefun.flink.core.StatefulFunctionsConfig;
import org.apache.flink.statefun.flink.core.StatefulFunctionsJob;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint that starts a local Flink environment to run the given Stateful Functions application
 * in this process.
 */
public final class LocalEnvironmentEntrypoint {
  private static final Logger LOG = LoggerFactory.getLogger(LocalEnvironmentEntrypoint.class);

  private static final String MODULE_OPTION = "module";
  private static final String CONFIGURATION_OPTION = "set";

  public static void main(String[] args) throws Exception {
    final Configuration flinkConfiguration = parseConfiguration(args);
    ConfigurationValidator.validate(flinkConfiguration);

    final StreamExecutionEnvironment env =
        StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(flinkConfiguration);

    final StatefulFunctionsConfig stateFunConfig =
        StatefulFunctionsConfig.fromFlinkConfiguration(flinkConfiguration);
    stateFunConfig.setProvider(new ClassPathUniverseProvider());

    StatefulFunctionsJob.main(env, stateFunConfig);
  }

  private static Configuration parseConfiguration(String[] args) {
    final MultipleParameterTool parameterTool = MultipleParameterTool.fromArgs(args);
    final Configuration flinkConfiguration = createDefaultLocalEnvironmentFlinkConfiguration();
    parseModuleOption(parameterTool, flinkConfiguration);
    parseConfigurationOptions(parameterTool, flinkConfiguration);

    return flinkConfiguration;
  }

  private static void parseConfigurationOptions(
      MultipleParameterTool parameterTool, Configuration flinkConfiguration) {
    final Collection<String> configurationOptions =
        parameterTool.getMultiParameter(CONFIGURATION_OPTION);

    if (configurationOptions != null) {
      for (String configurationOption : configurationOptions) {
        final String[] splits = configurationOption.split("=");

        if (splits.length != 2) {
          throw new IllegalArgumentException(
              String.format(
                  "The '--%s' value must have the form 'key=value'", CONFIGURATION_OPTION));
        }

        final String key = splits[0];
        final String value = splits[1];
        LOG.info("Setting configuration value: {}={}", key, value);
        flinkConfiguration.setString(key, value);
      }
    }
  }

  private static void parseModuleOption(
      MultipleParameterTool parameterTool, Configuration flinkConfiguration) {
    final String module = parameterTool.get(MODULE_OPTION, "file:///module.yaml");

    LOG.info("Setting module.yaml to: {}", module);
    flinkConfiguration.set(StatefulFunctionsConfig.REMOTE_MODULE_NAME, module);
  }

  private static Configuration createDefaultLocalEnvironmentFlinkConfiguration() {
    final Configuration flinkConfiguration = new Configuration();
    flinkConfiguration.set(StateBackendOptions.STATE_BACKEND, "rocksdb");
    flinkConfiguration.set(CheckpointingOptions.INCREMENTAL_CHECKPOINTS, true);

    // reduce Flink's memory footprint a bit
    flinkConfiguration.set(TaskManagerOptions.MANAGED_MEMORY_SIZE, MemorySize.ofMebiBytes(64));
    flinkConfiguration.set(TaskManagerOptions.NETWORK_MEMORY_MIN, MemorySize.ofMebiBytes(16));
    flinkConfiguration.set(TaskManagerOptions.NETWORK_MEMORY_MAX, MemorySize.ofMebiBytes(16));

    return flinkConfiguration;
  }
}
