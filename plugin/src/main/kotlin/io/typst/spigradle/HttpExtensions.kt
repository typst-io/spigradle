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

import java.io.OutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.DigestInputStream
import java.security.MessageDigest
import java.time.Duration
import java.util.HexFormat

internal val DEFAULT_CONNECT_TIMEOUT: Duration = Duration.ofSeconds(30)

/**
 * The timeout until the response headers are received; reading the body isn't limited by this,
 * so large downloads on a slow network don't fail.
 */
internal val DEFAULT_REQUEST_TIMEOUT: Duration = Duration.ofSeconds(60)

// HttpClient is thread-safe and pools connections, so share a single client across requests
private val httpClient: HttpClient by lazy {
    HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
        .build()
}

internal fun <A> fetchHttpGet(
    uri: URI,
    handler: HttpResponse.BodyHandler<A>,
    timeout: Duration = DEFAULT_REQUEST_TIMEOUT,
): HttpResponse<A> {
    val req = HttpRequest.newBuilder()
        .uri(uri)
        .header("User-Agent", "spigradle")
        .timeout(timeout)
        .GET()
        .build()

    val response = httpClient.send(req, handler)
    if (response.statusCode() / 100 != 2) {
        throw IllegalStateException("Download failed: HTTP $uri ${response.statusCode()}")
    }
    return response
}

internal fun fetchHttpGetAsString(uri: URI): HttpResponse<String> =
    fetchHttpGet(uri, HttpResponse.BodyHandlers.ofString())

internal fun fetchHttpGetAsByteArray(uri: URI): HttpResponse<ByteArray> =
    fetchHttpGet(uri, HttpResponse.BodyHandlers.ofByteArray())

/**
 * Downloads [uri] into [target] atomically.
 *
 * The body is streamed into a temporary file next to [target], and moved to [target] only after the download
 * completes and matches [sha256] if given, so [target] is never left partially written or corrupted.
 */
internal fun fetchHttpGetToFile(uri: URI, target: Path, sha256: String? = null) {
    val dir = target.toAbsolutePath().parent
    Files.createDirectories(dir)
    val temp = Files.createTempFile(dir, "${target.fileName}.", ".part")
    try {
        fetchHttpGet(uri, HttpResponse.BodyHandlers.ofFile(temp))
        if (sha256 != null) {
            val actual = sha256Of(temp)
            if (!actual.equals(sha256, ignoreCase = true)) {
                throw IllegalStateException("Checksum mismatch: $uri, expected sha256 $sha256 but was $actual")
            }
        }
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
        }
    } finally {
        Files.deleteIfExists(temp)
    }
}

internal fun sha256Of(file: Path): String {
    val digest = MessageDigest.getInstance("SHA-256")
    DigestInputStream(Files.newInputStream(file), digest).use {
        it.transferTo(OutputStream.nullOutputStream())
    }
    return HexFormat.of().formatHex(digest.digest())
}
