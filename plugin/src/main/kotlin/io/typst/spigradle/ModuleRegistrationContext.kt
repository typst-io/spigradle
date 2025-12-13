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

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

data class ModuleRegistrationContext<A>(
    val platformName: String,
    val descFileName: String,
    val extension: A,
    val mainDetectOutputFile: Provider<RegularFile>,
    val descGenTask: String,
    val mainDetectTask: String,
    val mainSuperClass: String = "",
)
