package br.com.eduardoenemark.pjrw.app.server.resource

import br.com.eduardoenemark.pjrw.app.server.AppServerInitializer
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.springframework.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification

//TODO: Making tests...
class AppServerSpec extends Specification {


    final def SERVER_PORT = 8080
    final def SERVER_URL_BASE = "http://localhost:${SERVER_PORT}"

    @Shared
    final def appServerThread = new Thread({
        AppServerInitializer.main(new String[]{"-Dserver.port=${SERVER_PORT}"})
    })

    def setupSpec() {
        appServerThread.start()
        Thread.sleep(10_000)
    }

    def cleanupSpec() {
        appServerThread.interrupt()
    }

    def "count product entries"() {
        given:
        def client = new OkHttpClient()
        def request = new Request.Builder()
                .url("${SERVER_URL_BASE}/products/count")
                .get()
                .build()

        Call call = client.newCall(request)
        Response response = call.execute();

        expect:
        response.code() == HttpStatus.OK.value()

        cleanup:
        response.close()
    }
}