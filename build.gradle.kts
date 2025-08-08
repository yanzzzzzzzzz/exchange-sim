allprojects {
    repositories { mavenCentral() }
}

subprojects {
    // 套用 Java plugin（動態）
    apply(plugin = "java")

    // 正確取得 Java 擴充，設定 Toolchain = JDK 21
    extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    // 以名稱加入相依（避免型別安全 accessor）
    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter:5.10.1")
    }

    // 用型別篩選設定 Test 任務
    tasks.withType(org.gradle.api.tasks.testing.Test::class.java).configureEach {
        useJUnitPlatform()
    }
}
