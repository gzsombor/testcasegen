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

import java.math.BigDecimal;

public class NumberSourceCode extends SourceCodeGenerator {

    private final Number number;

    public NumberSourceCode(Number number) {
        this.number = number;
    }

    @Override
    public String getObjectAccess() {
        if (number instanceof BigDecimal) {
            BigDecimal bd = ((BigDecimal)number);
            try {
                long value = bd.longValueExact();
                if (value == 0) {
                    return "BigDecimal.ZERO";
                }
                if (value == 1) {
                    return "BigDecimal.ONE";
                }
                if (value == 10) {
                    return "BigDecimal.TEN";
                }
                return "BigDecimal.valueOf(" + value + ")";
            } catch (ArithmeticException ae) {
                return "new BigDecimal(\"" + ((BigDecimal)number).toString() + "\")";
            }
        } else if (number instanceof Long) {
            return number.toString() + "l";
        } else if (number instanceof Float) {
            return number.toString() + "f";
        } else if (number instanceof Double) {
            return number.toString() + "d";
        }
        return number.toString();
    }

}
