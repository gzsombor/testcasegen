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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.gzsombor.testcasegen.src.SourceCodeGenerator;

public class TestcaseSourceFormatter {

    private final static String PADDING = "    ";

    final TestcaseGenerator status;
    final PrintWriter out;
    final Set<Object> publicObjects = new HashSet<>();

    public TestcaseSourceFormatter(TestcaseGenerator status, PrintWriter out) {
        this.status = status;
        this.out = out;
    }

    public void markPublic(Object... objects) {
        publicObjects.addAll(Arrays.asList(objects));
    }

    public void write(String name) {
        List<SourceCodeGenerator> results = status.getIntrospectionResults();
        out.println("public class " + name + " { ");
        for (SourceCodeGenerator intro : results) {
            String variableDeclaration = intro.getCacheVariableDeclaration();
            if (variableDeclaration != null) {
                out.println(PADDING + variableDeclaration + ";");
            }
        }
        out.println();
        for (SourceCodeGenerator intro : results) {
            if (intro.isHasInitializer()) {
                out.println(intro.getInitializer(status, PADDING));
            }
        }
        out.println("}");
    }

    public static String dumpCode(TestcaseGenerator generator, String className) {
        final StringWriter out = new StringWriter();
        final TestcaseSourceFormatter formatter = new TestcaseSourceFormatter(generator, new PrintWriter(out));
        formatter.write(className);
        return out.toString();
    }
}
