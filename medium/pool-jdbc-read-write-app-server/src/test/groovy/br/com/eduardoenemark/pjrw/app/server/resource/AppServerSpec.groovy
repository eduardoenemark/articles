package br.com.eduardoenemark.pjrw.app.server.resource

import br.com.eduardoenemark.pjrw.app.server.AppServerInitializer
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.springframework.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification

import static br.com.eduardoenemark.pjrw.app.server.config.AppConfiguration.LOGGER

//TODO: Making tests...
class AppServerSpec extends Specification {

    @Shared
    final def SERVER_PORT = 8080
    @Shared
    final def SERVER_URL_BASE = "http://localhost:${SERVER_PORT}"
    @Shared
    final def PING_URL = "${SERVER_URL_BASE}/ping"
    @Shared
    final def appServerThread = new Thread({
        AppServerInitializer.main(new String[]{"-Dserver.port=${SERVER_PORT}"})
    })

    def setupSpec() {
        appServerThread.start()
        final def client = new OkHttpClient()
        final def request = new Request.Builder().url(PING_URL).get().build()
        while (true) {
            Call call = client.newCall(request)
            LOGGER.info("Invoking ping URL: ${PING_URL}")
            try {
                try (Response response = call.execute()) {
                    LOGGER.info("response status: ${response.code()}")
                    if (call.executed && response.successful) {
                        LOGGER.info("App Server is ready!")
                        break
                    }
                }
            } catch (ConnectException ex) {
                LOGGER.error("App Server is offline: {}", ex.message)
            }
            LOGGER.info("Waiting 1s...")
            Thread.sleep(1_000)
        }
    }

    def cleanupSpec() {
        if (!appServerThread.interrupted) {
            appServerThread.interrupt()
        }
    }

    def "count product entries"() {
        given:
        def client = new OkHttpClient()
        def request = new Request.Builder()
                .url("${SERVER_URL_BASE}/products/count")
                .get()
                .build()

        Call call = client.newCall(request)
        Response response = call.execute()

        expect:
        response.code() == HttpStatus.OK.value()

        cleanup:
        response.close()
    }
}