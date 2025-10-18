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

internal fun <A> emptyDirectedGraph(): DirectedGraph<A> = DirectedGraph(emptyMap())

data class DirectedGraph<A>(val map: Map<A, Set<A>> = emptyMap()) {
    fun addNode(a: A): DirectedGraph<A> {
        val set = map[a] ?: emptySet()
        return copy(map = map + (a to set))
    }

    fun addEdge(parent: A, child: A): DirectedGraph<A> {
        val childSet = (map[child] ?: emptySet())
        val parentSet = (map[parent] ?: emptySet()) + child
        return copy(
            map = map + (child to childSet) + (parent to parentSet)
        )
    }

    fun getChildren(a: A): Set<A> {
        return map[a] ?: emptySet()
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
