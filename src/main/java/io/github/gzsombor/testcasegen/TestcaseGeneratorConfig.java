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
package io.github.gzsombor.testcasegen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestcaseGeneratorConfig {

    Map<Class<?>, Introspector> builtinIntrospector = new HashMap<>();

    Set<Class<?>> skipClasses = new HashSet<>();

    public TestcaseGeneratorConfig() {
    }

    public IntrospectionPlan addDefaultIntrospector(Class<?> type) {
        IntrospectionPlan plan = new IntrospectionPlan(type);
        addIntrospector(type, plan);
        return plan;
    }

    public void addSkipClass(Class<?> type) {
        this.skipClasses.add(type);
    }

    public void addSkipClasses(Class<?>... types) {
        this.skipClasses.addAll(Arrays.asList(types));
    }

    public void addIntrospector(Class<?> type, Introspector intros) {
        this.builtinIntrospector.put(type, intros);
    }

    public Introspector getIntrospector(Class<?> type) {
        return builtinIntrospector.get(type);
    }

    public TestcaseGenerator startIntrospect() {
        return new TestcaseGenerator(this);
    }
}
