/*
 * Copyright (c) 2025 Spigradle contributors.
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

package io.typst.spigradle

import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings

internal data class YamlValue(val value: Any?) {
    internal fun get(key: String): YamlValue {
        if (value is Map<*, *>) {
            val theValue = value[key]
            return theValue as? YamlValue ?: YamlValue(theValue)
        }
        return YamlValue(null)
    }

    internal fun asList(): List<YamlValue>? {
        return if (value is List<*>) {
            return value.map { YamlValue(it) }
        } else null
    }

    internal fun asMap(): Map<String, YamlValue>? {
        return if (value is Map<*, *>) {
            value.entries.associate {
                it.key.toString() to YamlValue(it.value)
            }
        } else null
    }

    companion object {
        internal fun parse(xs: String): YamlValue {
            val loadSettings = LoadSettings.builder()
                .build()
            val load = Load(loadSettings)
            return YamlValue(load.loadFromString(xs))
        }
    }
}
