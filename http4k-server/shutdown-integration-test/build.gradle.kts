description = "Integration tests for all servers"

plugins {
    id("org.http4k.conventions")
    application
}

application {
    mainClass.set("org.http4k.testing.TestServerKt")
}

dependencies {
    api(project(":http4k-core"))
    api(project(":http4k-realtime-core"))
    implementation(project(":http4k-server-apache"))
    implementation(project(":http4k-server-apache4"))
    implementation(project(":http4k-server-undertow"))
    implementation(project(":http4k-server-ratpack"))
    implementation(project(":http4k-server-helidon"))
    implementation(project(":http4k-server-jetty"))
    implementation(project(":http4k-server-netty"))
    implementation(project(":http4k-server-ktornetty"))
    implementation(project(":http4k-server-ktorcio"))
    implementation(project(":http4k-cloudnative"))
    implementation(project(":http4k-format-jackson"))
    testImplementation(testFixtures(project(":http4k-core")))
    testImplementation(testFixtures(project(":http4k-realtime-core")))
    testImplementation("com.github.docker-java:docker-java-core:_")
    testImplementation("com.github.docker-java:docker-java-transport-httpclient5:_")
}

tasks.test {
    filter {
        exclude("integration/**")
    }
}

tasks.register<Test>("integrationTests") {
    description = "Runs docker-based server shutdown tests."
    group = "verification"

    useJUnitPlatform()

    filter {
        include("integration/**")
    }
}
