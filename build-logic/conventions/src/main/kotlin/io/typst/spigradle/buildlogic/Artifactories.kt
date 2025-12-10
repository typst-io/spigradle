/*
 * Copyright (c) 2020 Spigradle contributors.
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

package io.typst.spigradle.buildlogic

import groovy.lang.GroovyObject

var GroovyObject.repoKey
    get() = getProperty("repoKey")
    set(value) = setProperty("repoKey", value)

var GroovyObject.username
    get() = getProperty("username")
    set(value) = setProperty("username", value)

var GroovyObject.password
    get() = getProperty("password")
    set(value) = setProperty("password", value)

fun GroovyObject.publications(vararg names: String) = invokeMethod("publications", names)

var GroovyObject.publishPom
    get() = getProperty("publishPom").toString().toBoolean()
    set(value) = setProperty("publishPom", value)

var GroovyObject.publishArtifacts
    get() = getProperty("publishArtifacts").toString().toBoolean()
    set(value) = setProperty("publishArtifacts", value)