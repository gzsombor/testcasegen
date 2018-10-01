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

public class EnumSourceCode extends SourceCodeGenerator {

    private final Enum<?> enumValue;

    public EnumSourceCode(Enum<?> enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String getObjectAccess() {
        return enumValue.getDeclaringClass().getSimpleName() + '.' + enumValue.name();
    }

}
