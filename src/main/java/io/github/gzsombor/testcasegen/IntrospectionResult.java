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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.gzsombor.testcasegen.src.SourceCodeGenerator;

public class IntrospectionResult extends SourceCodeGenerator {
    private final Class<?> type;
    private final int counter;
    private boolean useCounter = true;

    private Map<String, Object> attributes = new HashMap<>();

    private List<Object> referredObjects = new ArrayList<>();

    public IntrospectionResult(Class<?> type, int counter) {
        this.type = type;
        this.counter = counter;
    }
    
    public void setUseCounter(boolean useCounter) {
        this.useCounter = useCounter;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
        if (!isSimple(value)) {
            if (value instanceof Collection) {
                referredObjects.addAll((Collection) value);
            } else {
                referredObjects.add(value);
            }
        }
    }

    private boolean isSimple(Object result) {
        if (result == null) {
            return true;
        }
        Class resultType = result.getClass();
        return (resultType.isPrimitive() || resultType.isEnum() || String.class.equals(resultType) || result instanceof Number);
    }

    @Override
    public List<Object> getReferredObjects() {
        return referredObjects;
    }

    @Override
    public boolean isHasInitializer() {
        return true;
    }

    @Override
    public String getInitializer(TestcaseGenerator ctx, String padding) {
        StringBuilder out = new StringBuilder();
        final String visibility = publicFlag ? "public" : "private";
        out.append(padding).append(visibility).append(" " + type.getSimpleName() + ' ' + getMethodName() + "() {\n");
        final String var = getCacheVariableName();
        out.append(padding).append("    if (" + var + " == null) {\n");
        out.append(padding).append("         this." + var + " = new " + type.getSimpleName() + "();\n");
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                if (value.getClass().isPrimitive() || value instanceof Boolean) {
                    callSetter(padding, out, var, entry.getKey(), value);
                } else if (value instanceof String) {
                    callSetter(padding, out, var, entry.getKey(), '"' + (((String) value).replaceAll("\\\"", "'")) + '"');
                } else {
                    SourceCodeGenerator codeGenerator = ctx.getSourceCodeGenerator(value);
                    if (codeGenerator != null) {
                        callSetter(padding, out, var, entry.getKey(), codeGenerator.getObjectAccess());
                    } else {
                        out.append(padding).append("         // skipping setting " + entry.getKey() + " to " + value + "\n");
                        // callSetter(padding, out, var, entry.getKey(), "/* ?
                        // */" + value);
                    }
                }
            }
        }
        out.append(padding).append("    }\n");
        out.append(padding).append("    return " + getCacheVariableName() + ";\n");
        out.append(padding).append("}\n");
        return out.toString();
    }

    /**
     * @param padding
     * @param out
     * @param var
     * @param entry
     * @param value
     */
    private void callSetter(String padding, StringBuilder out, final String var, String key, Object value) {
        out.append(padding).append("         this." + var + ".set" + key + "(").append(value).append(");\n");
    }

    @Override
    public String getCacheVariableDeclaration() {
        return "private " + type.getSimpleName() + " " + getCacheVariableName();
    }

    public String getCacheVariableName() {
        return toVariableName(type.getSimpleName()) + (useCounter ? counter : "");
    }

    @Override
    public String getOrdering() {
        return type.getSimpleName() + counter;
    }

    private static String toVariableName(String className) {
        return new StringBuilder().append(Character.toLowerCase(className.charAt(0))).append(className.substring(1)).toString();
    }

    public String getMethodName() {
        return "get" + type.getSimpleName() + (useCounter ? counter : "");
    }

    @Override
    public String getObjectAccess() {
        return getMethodName() + "()";
    }
}
