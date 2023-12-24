package tictactoe.acceptance

import bed.mgmt.fixtures.containers.Postgres
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser.*
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import tictactoe.acceptance.PlayTicTacToeGame.*
import tictactoe.shell.App

@Tag("acceptance")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = [Initializer::class], classes = [App::class])
class PlayTicTacToeGame {

    companion object {
        val postgres = Postgres()
    }

    init {
        RestAssured.defaultParser = JSON
    }

    @LocalServerPort
    protected val servicePort: Int = 0

    @Tag("acceptance")
    @Test
    fun `should play a game`() {
        val id: String = RestAssured
            .given().port(servicePort)
            .`when`().post("/games")
            .then().assertThat().statusCode(201).extract().path("id")
        RestAssured
            .given().port(servicePort).body("""{"player": "X", "col": 1, "row": 1}""").contentType(ContentType.JSON)
            .`when`().post("/games/$id/marks")
            .then().assertThat().statusCode(201)
        RestAssured
            .given().port(servicePort).body("""{"player": "O", "col": 2, "row": 1}""").contentType(ContentType.JSON)
            .`when`().post("/games/$id/marks")
            .then().assertThat().statusCode(201)
        RestAssured
            .given().port(servicePort).body("""{"player": "X", "col": 1, "row": 2}""").contentType(ContentType.JSON)
            .`when`().post("/games/$id/marks")
            .then().assertThat().statusCode(201)
        RestAssured
            .given().port(servicePort).body("""{"player": "O", "col": 2, "row": 2}""").contentType(ContentType.JSON)
            .`when`().post("/games/$id/marks")
            .then().assertThat().statusCode(201)
        RestAssured
            .given().port(servicePort).body("""{"player": "X", "col": 1, "row": 3}""").contentType(ContentType.JSON)
            .`when`().post("/games/$id/marks")
            .then().assertThat().statusCode(201)
        RestAssured
            .given().port(servicePort)
            .`when`().get("/games/$id")
            .then()
            .assertThat().statusCode(200)
            .body(equalTo("""{"id":"$id","board":[["X","O",""],["X","O",""],["X","",""]],"status":"WIN","turn":null,"winner":"X"}"""))

    }


    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=" + postgres.container.jdbcUrl,
                "spring.datasource.password=" + postgres.container.password,
                "spring.datasource.username=" + postgres.container.username,
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}
