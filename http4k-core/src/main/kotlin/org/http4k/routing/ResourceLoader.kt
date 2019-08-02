package org.http4k.routing

import java.io.File
import java.net.URL

interface ResourceLoader {
    fun load(path: String): URL?

    companion object {
        fun Classpath(basePackagePath: String = "/", muteWarning: Boolean = false) = object : ResourceLoader {

            private val withStarting = if (basePackagePath.startsWith("/")) basePackagePath else "/$basePackagePath"

            private val finalBasePath = if (withStarting.endsWith("/")) withStarting else "$withStarting/"

            init {
                if (!muteWarning && finalBasePath == "/") {
                    System.err.println("WARNING - http4k Classpath ResourceLoader is configured to serve ALL files " +
                        "from the root of the Java classpath.\n" +
                        "For security you should serve from a non-code package eg. /public, " +
                        "or mute this warning using the flag on construction.")
                }
            }

            override fun load(path: String): URL? = javaClass.getResource(finalBasePath + path)
        }

        fun Directory(baseDir: String = ".") = object : ResourceLoader {
            private val finalBaseDir = if (baseDir.endsWith("/")) baseDir else "$baseDir/"

            override fun load(path: String): URL? =
                File(finalBaseDir, path).let { f -> if (f.exists() && f.isFile) f.toURI().toURL() else null }
        }

    }
}