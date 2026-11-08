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
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf

internal fun <A> emptyDirectedGraph(): DirectedGraph<A> = DirectedGraph(persistentMapOf())

internal data class DirectedGraph<A>(val map: PersistentMap<A, PersistentSet<A>> = persistentMapOf()) {
    fun addNode(a: A): DirectedGraph<A> {
        if (a in map) return this
        val set = map[a] ?: persistentSetOf()
        return copy(map = map.putting(a, set))
    }

    fun addEdge(parent: A, child: A): DirectedGraph<A> {
        val childSet = (map[child] ?: persistentSetOf())
        val parentSet = (map[parent] ?: persistentSetOf()).adding(child)
        return copy(
            map = map.putting(child, childSet).putting(parent, parentSet)
        )
    }

    fun getChildren(a: A): Set<A> {
        return map[a] ?: persistentSetOf()
    }

    fun getAllChildren(a: A): Set<A> {
        val ret = mutableSetOf<A>()
        val stack = ArrayDeque<A>()
        stack.addAll(getChildren(a))
        while (stack.isNotEmpty()) {
            val a = stack.removeLast()
            if (ret.add(a)) {
                stack.addAll(getChildren(a))
            }
        }
        return ret
    }
}
