package au.com.skater901.wc3

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.api.core.domain.Region
import au.com.skater901.wc3.api.core.service.AdminMessageNotifier
import au.com.skater901.wc3.api.core.service.GameNotifier
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.api.scheduled.ScheduledTask
import au.com.skater901.wc3.core.domain.WC3StatsGame
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern
import com.github.tomakehurst.wiremock.matching.UrlPattern
import com.google.inject.BindingAnnotation
import com.google.inject.Injector
import com.marcinziolo.kotlin.wiremock.*
import io.dropwizard.testing.junit5.DropwizardAppExtension
import io.dropwizard.testing.junit5.DropwizardClientExtension
import jakarta.ws.rs.*
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.client.ClientRequestFilter
import jakarta.ws.rs.client.Entity.json
import jakarta.ws.rs.client.Entity.text
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.UriBuilder
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import ru.vyarus.dropwizard.guice.module.installer.scanner.InvisibleForScanner
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URI
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

@Testcontainers
@WireMockTest
class WC3NotificationBotE2EITCase {
    class TestConfiguration(val testProperty: String)

    class TestGameNotifier : GameNotifier {
        override suspend fun notifyNewGame(notificationId: String, game: Game) {
            notificationClient.target(UriBuilder.fromPath("/notify/{notificationId}/new").build(notificationId))
                .request()
                .post(json(game), String::class.java)
        }

        override suspend fun updateExistingGame(game: Game) {
            notificationClient.target("/notify/update")
                .request()
                .post(json(game), String::class.java)
        }

        override suspend fun closeExpiredGame(game: Game) {
            notificationClient.target("/notify/closed")
                .request()
                .post(json(game), String::class.java)
        }
    }

    class TestScheduledTask : ScheduledTask {
        override val schedule: Int = 5

        override suspend fun task() {
            scheduledTaskRun = true
        }
    }

    @InvisibleForScanner
    @Path("/")
    class TestNotificationHandlerResource(
        @param:TestModuleAnnotation
        private val wc3GameNotificationService: WC3GameNotificationService,
        private val config: TestConfiguration
    ) {
        @POST
        @Path("/register/{id}")
        fun register(@PathParam("id") id: String, body: Map<String, String>) {
            runBlocking {
                wc3GameNotificationService.createNotification(id, body["requestPattern"]!!)
            }
        }

        @DELETE
        @Path("/register/{id}")
        fun delete(@PathParam("id") id: String) {
            runBlocking { wc3GameNotificationService.deleteNotification(id) }
        }

        @GET
        @Path("/config")
        @Produces(MediaType.TEXT_PLAIN)
        fun configValue(): String = config.testProperty
    }

    @BindingAnnotation
    annotation class TestModuleAnnotation

    class AdminNotifier : AdminMessageNotifier {
        override suspend fun sendAdminMessage(message: String, notifications: List<String>) {
            notifications.forEach {
                notificationClient.target(UriBuilder.fromPath("/admin/{id}").build(it))
                    .request()
                    .post(text(message), String::class.java)
            }
        }
    }

    class TestModule : NotificationModule<TestConfiguration> {
        override val moduleName: String = "test-module"
        override val configClass: KClass<TestConfiguration> = TestConfiguration::class
        override val annotation: KClass<out Annotation> = TestModuleAnnotation::class

        override fun initializeNotificationHandlers(
            config: TestConfiguration,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
            testNotificationHandler = DropwizardClientExtension(
                TestNotificationHandlerResource(
                    wc3GameNotificationService,
                    config
                )
            )
                .apply { before() }
        }

        override val gameNotifier: KClass<out GameNotifier> = TestGameNotifier::class

        override val scheduledTask: KClass<out ScheduledTask> = TestScheduledTask::class

        override val adminMessageNotifier: KClass<out AdminMessageNotifier> = AdminNotifier::class
    }

    class TestConfiguration2

    class TestGameNotifier2 : GameNotifier {
        override suspend fun notifyNewGame(notificationId: String, game: Game) {
        }

        override suspend fun updateExistingGame(game: Game) {
        }

        override suspend fun closeExpiredGame(game: Game) {
        }
    }

    @BindingAnnotation
    annotation class TestModuleAnnotation2

    class TestModule2 : NotificationModule<TestConfiguration2> {
        override val moduleName: String = "test-module2"
        override val configClass: KClass<TestConfiguration2> = TestConfiguration2::class
        override val annotation: KClass<out Annotation> = TestModuleAnnotation2::class

        override fun initializeNotificationHandlers(
            config: TestConfiguration2,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
        }

        override val gameNotifier: KClass<out GameNotifier> = TestGameNotifier2::class
    }

    companion object {
        lateinit var testNotificationHandler: DropwizardClientExtension
        lateinit var notificationClient: Client
            private set

        var scheduledTaskRun = false

        @Container
        private val database = MySQLContainer("mysql")
            .withUsername("wc3_bot")
            .withPassword("wc3_bot")
            .withDatabaseName("wc3_bot")

        private val app = DropwizardAppExtension<WC3NotificationBotConfiguration>(
            WC3NotificationBot::class.java,
            "conf/wc3-notification-bot.yml"
        )

        @BeforeAll
        @JvmStatic
        fun setUp(wireMock: WireMockRuntimeInfo) {
            Properties().apply {
                load(FileInputStream("conf/e2e/wc3-notification-bot.properties"))
                set("database.host", database.host)
                set("database.port", database.firstMappedPort.toString())
                set("wc3connect.baseUri", wireMock.httpBaseUrl)
                set("wc3stats.baseUri", wireMock.httpBaseUrl)
                set("test-module.testProperty", "potato")
                File("build/conf/e2e").mkdirs()
                store(FileOutputStream("build/conf/e2e/wc3-notification-bot.properties"), null)
            }

            notificationClient = ClientBuilder.newClient()
                .register(JacksonJsonProvider(ObjectMapper().registerModule(JavaTimeModule()).registerKotlinModule()))
                .register(ClientRequestFilter { request ->
                    request.uri = URI.create(wireMock.httpBaseUrl + request.uri.toString())
                })

            System.setProperty("configFile", "build/conf/e2e/wc3-notification-bot.properties")
            System.setProperty("enabledModules", "test-module,test-module2")

            app.before()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            app.after()
            testNotificationHandler.after()
            notificationClient.close()
        }
    }

    @Test
    fun `should register notification, notify about game being created, updated, started, then delete notification`(
        wireMock: WireMockRuntimeInfo
    ) {
        ClientBuilder.newBuilder()
            .build()
            .use { client ->
                client.target(testNotificationHandler.baseUri().toString() + "/config")
                    .request(MediaType.TEXT_PLAIN)
                    .get(String::class.java)
                    .also { assertThat(it).isEqualTo("potato") }

                client.target(testNotificationHandler.baseUri().toString() + "/register/test")
                    .request()
                    .post(json(mapOf("requestPattern" to "^Swat")), String::class.java)

                val mapper = ObjectMapper().registerModule(JavaTimeModule())
                    .registerKotlinModule()

                val created = Instant.now()

                wireMock.wireMock.apply {
                    resetMappings()

                    post {
                        url equalTo "/notify/test/new"
                    } returns {}
                    post {
                        url equalTo "/notify/update"
                    } returns {}
                    post {
                        url equalTo "/notify/closed"
                    } returns {}

                    get {
                        url equalTo "/allgames"

                        headers contains HttpHeaders.ACCEPT equalTo MediaType.APPLICATION_JSON
                    } returns {
                        header = HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON
                        body = "[]"
                    }

                    get {
                        whenState = null
                        toState = "game created"
                        url equalTo "/gamelist"
                    } returns {
                        header = HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON
                        body = mapper.writeValueAsString(
                            mapOf(
                                "body" to listOf(
                                    mapOf(
                                        "id" to 1,
                                        "name" to "private swat",
                                        "map" to "SwatAfterP241127",
                                        "host" to "teller55",
                                        "slotsTaken" to 1,
                                        "slotsTotal" to 9,
                                        "created" to created,
                                        "server" to "usw"
                                    )
                                )
                            )
                        )
                    }
                    get {
                        whenState = "game created"
                        toState = "game updated"
                        url equalTo "/gamelist"
                    } returns {
                        header = HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON
                        body = mapper.writeValueAsString(
                            mapOf(
                                "body" to listOf(
                                    mapOf(
                                        "id" to 1,
                                        "name" to "private swat",
                                        "map" to "SwatAfterP241127",
                                        "host" to "teller55",
                                        "slotsTaken" to 7,
                                        "slotsTotal" to 9,
                                        "created" to created,
                                        "server" to "usw"
                                    )
                                )
                            )
                        )
                    }
                    get {
                        whenState = "game updated"
                        toState = "game closed"
                        url equalTo "/gamelist"
                    } returns {
                        header = HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON
                        body = mapper.writeValueAsString(mapOf("body" to emptyList<Any>()))
                    }
                }

                await().atMost(1, TimeUnit.MINUTES)
                    .untilAsserted {
                        wireMock.wireMock.apply {
                            verify {
                                method = RequestMethod.POST
                                url equalTo "/notify/test/new"

                                body equalTo mapper.writeValueAsString(
                                    WC3StatsGame(
                                        1,
                                        "private swat",
                                        "SwatAfterP241127",
                                        "teller55",
                                        1,
                                        9,
                                        created,
                                        Region.US
                                    )
                                )
                            }
                            verify {
                                method = RequestMethod.POST
                                url equalTo "/notify/update"

                                body equalTo mapper.writeValueAsString(
                                    WC3StatsGame(
                                        1,
                                        "private swat",
                                        "SwatAfterP241127",
                                        "teller55",
                                        7,
                                        9,
                                        created,
                                        Region.US
                                    )
                                )
                            }
                            verify {
                                method = RequestMethod.POST
                                url equalTo "/notify/closed"

                                body equalTo mapper.writeValueAsString(
                                    WC3StatsGame(
                                        1,
                                        "private swat",
                                        "SwatAfterP241127",
                                        "teller55",
                                        7,
                                        9,
                                        created,
                                        Region.US
                                    )
                                )
                            }
                        }
                    }

                wireMock.wireMock.post {
                    url equalTo "/admin/test"
                } returns {}

                client.target("http://localhost:8080/admin")
                    .request()
                    .post(text("admin message"), String::class.java)

                await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted {
                        wireMock.wireMock.verifyThat(
                            newRequestPattern(RequestMethod.POST, UrlPattern(EqualToPattern("/admin/test"), false))
                                .withRequestBody(EqualToPattern("admin message"))
                        )
                    }

                client.target(testNotificationHandler.baseUri().toString() + "/register/test")
                    .request()
                    .delete(String::class.java)

                wireMock.wireMock.apply {
                    resetRequests()

                    get {
                        url equalTo "/gamelist"
                    } returns {
                        header = HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON
                        body = mapper.writeValueAsString(
                            mapOf(
                                "body" to listOf(
                                    mapOf(
                                        "id" to 1,
                                        "name" to "private swat",
                                        "map" to "SwatAfterP241127",
                                        "host" to "teller55",
                                        "slotsTaken" to 1,
                                        "slotsTotal" to 9,
                                        "created" to created,
                                        "server" to "usw"
                                    )
                                )
                            )
                        )
                    }
                }

                Thread.sleep(15_000)

                wireMock.wireMock.verify {
                    method = RequestMethod.POST
                    url equalTo "/notify/test/new"
                    exactly = 0
                }

                wireMock.wireMock.verify {
                    method = RequestMethod.POST
                    url equalTo "/notify/updated"
                    exactly = 0
                }

                wireMock.wireMock.verify {
                    method = RequestMethod.POST
                    url equalTo "/notify/closed"
                    exactly = 0
                }
            }

        assertThat(scheduledTaskRun).isTrue()
    }
}