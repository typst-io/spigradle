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

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

internal fun <A> fetchHttpGet(uri: URI, handler: HttpResponse.BodyHandler<A>): HttpResponse<A> {
    val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()
    val req = HttpRequest.newBuilder()
        .uri(uri)
        .header("User-Agent", "spigradle")
        .GET()
        .build()

    val response = client.send(req, handler)
    if (response.statusCode() / 100 != 2) {
        throw IllegalStateException("Download failed: HTTP ${response.statusCode()}")
    }
    return response
}

internal fun fetchHttpGetAsString(uri: URI): HttpResponse<String> =
    fetchHttpGet(uri, HttpResponse.BodyHandlers.ofString())

internal fun fetchHttpGetAsByteArray(uri: URI): HttpResponse<ByteArray> =
    fetchHttpGet(uri, HttpResponse.BodyHandlers.ofByteArray())
