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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.gzsombor.testcasegen.src.SourceCodeGenerator;

public class IntrospectionPlan implements Introspector {

    private final Class<?> type;
    private int counter;

    private Set<String> skippedAttributes;

    public IntrospectionPlan(Class<?> type) {
        this.type = type;
    }

    public void addSkipAttributes(String... attributes) {
        if (this.skippedAttributes == null) {
            this.skippedAttributes = new HashSet<>();
        }
        this.skippedAttributes.addAll(Arrays.asList(attributes));
    }

    @Override
    public SourceCodeGenerator introspect(Object obj) throws ReflectionException {
        IntrospectionResult result = new IntrospectionResult(type, counter++);
        Map<String, Class<?>> setters = new HashMap<>();
        for (Method method : type.getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if ((parameterTypes.length == 1) && method.getName().startsWith("set")) {
                setters.put(method.getName().substring(3), parameterTypes[0]);
            }
        }
        for (Method method : type.getMethods()) {
            if (method.getParameterTypes().length == 0 && !Void.TYPE.equals(method.getReturnType())) {
                String propertyName = getPropertyName(method);
                if (propertyName != null) {
                    Class<?> setterType = setters.get(propertyName);
                    if (setterType != null && setterType.equals(method.getReturnType())) {
                        collectAttributeValue(result, obj, method);
                    } else if (Collection.class.isAssignableFrom(method.getReturnType())) {
                        collectAttributeValue(result, obj, method);
                    }
                }
            }
        }
        return result;
    }
    
    private String getPropertyName(Method method) {
        String name = method.getName();
        if (name.startsWith("get")) {
            return name.substring(3);
        } else if (name.startsWith("is")) {
            return name.substring(2);
        }
        return null;
    }

    private void collectAttributeValue(IntrospectionResult result, Object obj, Method method) {
        if (method.getName().startsWith("get") && !Class.class.equals(method.getReturnType())) {
            addResult(result, obj, method.getName().substring(3), method);
        } else if (method.getName().startsWith("is") && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            addResult(result, obj, method.getName().substring(2), method);
        }
    }

    void addResult(IntrospectionResult introResult, Object obj, String name, Method method) throws ReflectionException {
        try {
            if (skippedAttributes != null && skippedAttributes.contains(name)) {
                return;
            }
            final Object result = method.invoke(obj);
            introResult.setAttribute(name, result);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ReflectionException(e);
        }
    }

}
