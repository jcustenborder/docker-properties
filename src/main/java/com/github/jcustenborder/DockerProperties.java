/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DockerProperties<VALUE> {
  private static final Logger log = LoggerFactory.getLogger(DockerProperties.class);
  final List<Pattern> patterns;
  final Map<String, String> environment;
  final Map<String, String> keyReplacements;
  final boolean lowerCaseKey;

  DockerProperties(List<Pattern> prefixes, Map<String, String> environment, Map<String, String> keyReplacements, boolean lowerCaseKey) {
    this.patterns = prefixes;
    this.environment = environment;
    this.keyReplacements = keyReplacements;
    this.lowerCaseKey = lowerCaseKey;
  }

  Map<String, VALUE> filter() {
    final Map<String, VALUE> result = new LinkedHashMap<>(this.environment.size());
    for (final Map.Entry<String, String> environmentEntry : this.environment.entrySet()) {
      log.trace("filter() - Processing '{}'", environmentEntry.getKey());

      for (final Pattern pattern : this.patterns) {
        log.trace("filter() - matching '{}' against '{}'", environmentEntry.getKey(), pattern.pattern());
        final Matcher matcher = pattern.matcher(environmentEntry.getKey());
        if (matcher.matches()) {
          final int groupCount = matcher.groupCount();
          if (groupCount != 1) {
            throw new IllegalStateException(
                String.format(
                    "Pattern '%s' returns the %s group(s) but should only return 1. Patterns must return a second group. EX '^KEY_(.+)$'",
                    pattern.pattern(),
                    groupCount
                )
            );
          }
          log.trace("filter() - matcher.matches() returned true for '{}' against '{}'", environmentEntry.getKey(), pattern.pattern());
          String key = matcher.group(1);
          for (final Map.Entry<String, String> replacement : this.keyReplacements.entrySet()) {
            key = key.replace(replacement.getKey(), replacement.getValue());
          }

          if (this.lowerCaseKey) {
            key = key.toLowerCase();
          }
          result.put(key, (VALUE) environmentEntry.getValue());
        } else {
          log.trace("filter() - matcher.matches() returned false for '{}' against '{}'", environmentEntry.getKey(), pattern.pattern());
        }
      }
    }
    return result;
  }

  public Map<String, VALUE> toMap() {
    return filter();
  }

  public Properties toProperties() {
    Map<String, VALUE> filtered = filter();
    Properties properties = new Properties();
    properties.putAll(filtered);
    return properties;
  }

  public static class Builder<VALUE> {
    Map<String, String> environment = System.getenv();
    List<Pattern> patterns = new ArrayList<>();
    Map<String, String> keyReplacements;
    boolean lowerCaseKey = true;

    Builder() {
      this.keyReplacements = new LinkedHashMap<>();
      this.keyReplacements.put("_", ".");
    }


    public Map<String, String> environment() {
      return environment;
    }

    public Builder environment(Map<String, String> environment) {
      if (null == environment) {
        throw new NullPointerException("environment cannot be null.");
      }
      this.environment = environment;
      return this;
    }

    public List<Pattern> patterns() {
      return this.patterns;
    }

    public Builder patterns(String pattern) {
      return patterns(pattern, 0);
    }

    public Builder patterns(String pattern, int flags) {
      Pattern p = Pattern.compile(pattern, flags);
      this.patterns.add(p);
      return this;
    }

    public DockerProperties<VALUE> build() {
      return new DockerProperties<>(this.patterns, this.environment, this.keyReplacements, this.lowerCaseKey);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
