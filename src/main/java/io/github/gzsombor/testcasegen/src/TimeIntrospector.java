/**
 * Copyright (C) 2018 Zsombor Gegesy (gzsombor@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.gzsombor.testcasegen.src;

import java.time.Instant;
import java.time.LocalDate;

import io.github.gzsombor.testcasegen.Introspector;
import io.github.gzsombor.testcasegen.ReflectionException;

public class TimeIntrospector implements Introspector {

    @Override
    public SourceCodeGenerator introspect(Object obj) throws ReflectionException {
        if (obj instanceof Instant) {
            return new InstantSourceCode((Instant) obj);
        }
        if (obj instanceof LocalDate) {
            return new LocalDateSourceCode((LocalDate) obj);
        }
        throw new ReflectionException("Unexpected type: " + obj);
    }

}
