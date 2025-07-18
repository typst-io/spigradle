/*
 * Copyright (c) 2022 Spigradle contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.typst.spigradle.detection

internal fun findSubClass(classDef: ClassDefinition, superClassNames: Set<String>): Pair<String?, Set<String>> {
    if (classDef.parentName in superClassNames) {
        val newSupers =
            if (classDef.abstractClass) {
                superClassNames + classDef.name
            } else superClassNames
        val result =
            if (classDef.publicClass && !classDef.abstractClass) {
                classDef.name
            } else null
        return result to newSupers
    }
    return null to superClassNames
}
