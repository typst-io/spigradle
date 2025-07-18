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

/**
 * Created by JunHyung Im on 2020-08-25
 */
data class PluginConvention(
        val serverName: String,
        val descFile: String,
        val mainSuperClass: String = "",
        val descGenTask: String = "generate${serverName.capitalized()}Description",
        val mainDetectTask: String = "detect${serverName.capitalized()}Main",
        val descExtension: String = serverName,
        val taskGroup: String = serverName
)
