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

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DockerPropertiesTest {

  @Test
  public void patterns() {
    final String pattern = "^FOO_(.+)$";
    DockerProperties.Builder<String> builder = DockerProperties.builder().patterns(pattern);
    assertFalse(builder.patterns().isEmpty(), "builder.patterns() should not be empty.");
    assertEquals(builder.patterns().get(0).pattern(), pattern);
  }

  @Test
  public void toMap() {
    final Map<String, String> environment = new LinkedHashMap<>();
    environment.put("FOO_BAR_BAZ", "This is a test");

    final Map<String, String> expected = new LinkedHashMap<>();
    expected.put("bar.baz", "This is a test");

    final Map<String, String> actual = DockerProperties.builder()
        .patterns("^FOO_(.+)$")
        .environment(environment)
        .build()
        .toMap();
    assertNotNull(actual);
    assertEquals(expected, actual);
  }
}
