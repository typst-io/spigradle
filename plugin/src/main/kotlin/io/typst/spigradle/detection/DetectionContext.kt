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

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

internal data class DetectionContext(
    val map: PersistentMap<String, ClassDefinition> = persistentMapOf(),
    val graph: DirectedGraph<String> = emptyDirectedGraph(),
) {
    fun addClassDef(x: ClassDefinition): DetectionContext {
        val newMap = map.putting(x.name, x)
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

    /**
     * Returns every public concrete class extending [parentName] directly or indirectly, sorted by the name,
     * so the result doesn't depend on the order the classes were added.
     */
    fun findMainClassCandidates(parentName: String): List<ClassDefinition> =
        graph.getAllChildren(parentName)
            .mapNotNull(map::get)
            .filter { it.publicClass && !it.abstractClass }
            .sortedBy { it.name }
}
