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

package io.typst.spigradle.detection

internal data class DetectionContext(
    val map: Map<String, ClassDefinition> = emptyMap(),
    val graph: DirectedGraph<String> = emptyDirectedGraph(),
) {
    fun addClassDef(x: ClassDefinition): DetectionContext {
        val newMap = map + (x.name to x)
        var newGraph = if (x.parentName != null) {
            graph.addEdge(x.parentName, x.name)
        } else graph.addNode(x.name)
        for (superInterface in x.interfaces) {
            newGraph = newGraph.addEdge(superInterface, x.name)
        }
        return copy(
            map = newMap,
            graph = newGraph
        )
    }

    fun findMainClass(parentName: String): ClassDefinition? {
        val children = graph.getAllChildren(parentName)
        return children
            .mapNotNull(map::get)
            .find {
                it.publicClass && !it.abstractClass
            }
    }
}
