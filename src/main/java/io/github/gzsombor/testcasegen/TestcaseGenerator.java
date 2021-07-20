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

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.gzsombor.testcasegen.src.CollectionSourceCodeGenerator;
import io.github.gzsombor.testcasegen.src.EnumSourceCode;
import io.github.gzsombor.testcasegen.src.InstantSourceCode;
import io.github.gzsombor.testcasegen.src.LocalDateSourceCode;
import io.github.gzsombor.testcasegen.src.NumberSourceCode;
import io.github.gzsombor.testcasegen.src.SourceCodeGenerator;
import io.github.gzsombor.testcasegen.src.StringSourceCode;
import io.github.gzsombor.testcasegen.src.TimeIntrospector;

public class TestcaseGenerator {

    TestcaseGeneratorConfig config;
    Map<Class<?>, Introspector> introspectedTypes = new HashMap<>();

    Map<Object, SourceCodeGenerator> introspectionStatus = new HashMap<>();

    public TestcaseGenerator(TestcaseGeneratorConfig config) {
        this.config = config;
        this.introspectedTypes = new HashMap<>();
    }

    public void introspect(Object obj) throws ReflectionException {
        walkList(Collections.singletonList(obj));
    }

    public void introspect(Object... obj) throws ReflectionException {
        walkList(Arrays.asList(obj));
    }

    private void walkList(List<Object> input) throws ReflectionException {
        List<Object> queue = new ArrayList<>(input);
        while (!queue.isEmpty()) {
            Object obj = queue.remove(queue.size() - 1);
            if (!introspectionStatus.containsKey(obj)) {
                Introspector plan = getPlan(obj.getClass());
                if (plan != null) {
                    SourceCodeGenerator result = plan.introspect(obj);
                    introspectionStatus.put(obj, result);
                    queue.addAll(result.getReferredObjects());
                }
            }
        }
        for (Object obj : input) {
            SourceCodeGenerator codeGenerator = introspectionStatus.get(obj);
            if (codeGenerator != null) {
                codeGenerator.setPublicFlag(true);
            }
        }
    }

    public List<SourceCodeGenerator> getIntrospectionResults() {
        Comparator<String> stringComp = Comparator.nullsLast((obj1, obj2) -> 
            obj1.compareTo(obj2)
        );
        return introspectionStatus.values().stream().sorted((s1, s2) -> stringComp.compare(s1.getOrdering(), s2.getOrdering())).collect(Collectors.toList());
    }

    /**
     * @param obj
     * @return
     */
    private Introspector getPlan(Class<?> type) {
        Introspector plan = introspectedTypes.get(type);
        if (plan == null) {
            plan = config.builtinIntrospector.get(type);
            if (plan != null) {
                return plan;
            }
            if (type.isEnum()) {
                return (object) -> new EnumSourceCode((Enum) object);
            }
            if (type.isPrimitive() || Number.class.isAssignableFrom(type)) {
                return (object) -> new NumberSourceCode((Number) object);
            }
            if (String.class.equals(type)) {
                return (object) -> new StringSourceCode((String) object);
            }
            if (Instant.class.equals(type)) {
                return new TimeIntrospector();
            }
            if (type.isArray()) {
                return null;
            }
            if (type.getPackage().getName().startsWith("java.")) {
                return null;
            }
            if (isHibernateProxy(type)) {
                // handle hibernate proxies
                return getPlan(type.getSuperclass());
            }
            if (check(type)) {
                plan = new IntrospectionPlan(type);
                introspectedTypes.put(type, plan);
            }
        }
        return plan;
    }

    private boolean isHibernateProxy(Class<?> type) {
        final String simpleName = type.getSimpleName();
        if (simpleName.contains("_$$_") && simpleName.startsWith(type.getSuperclass().getSimpleName())) {
            return true;
        }
        for (Class<?> iface : type.getInterfaces()) {
            if ("org.hibernate.proxy.HibernateProxy".equals(iface.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean check(Class<?> type) {
        System.out.println("Checking " + type);
        return !config.skipClasses.contains(type);
    }

    public SourceCodeGenerator getSourceCodeGenerator(Object value) {
        final SourceCodeGenerator scg = introspectionStatus.get(value);
        if (scg == null) {
            final Class type = value.getClass();
            if (type.isEnum()) {
                return new EnumSourceCode((Enum) value);
            }
            if (type.isPrimitive() || value instanceof Number) {
                return new NumberSourceCode((Number) value);
            }
            if (value instanceof String) {
                return new StringSourceCode((String) value);
            }
            if (value instanceof LocalDate) {
                return new LocalDateSourceCode((LocalDate) value);
            }
            if (value instanceof Instant) {
                return new InstantSourceCode((Instant) value);
            }
            if (value instanceof Collection) {
                final List<SourceCodeGenerator> scgList = ((Collection<Object>) value).stream().map(obj -> getSourceCodeGenerator(obj))
                        .filter(obj -> obj != null).collect(Collectors.toList());
                if (value instanceof Set) {
                    return new CollectionSourceCodeGenerator(scgList, true);
                } else {
                    return new CollectionSourceCodeGenerator(scgList, false);
                }
            }
        }
        return scg;
    }

}
