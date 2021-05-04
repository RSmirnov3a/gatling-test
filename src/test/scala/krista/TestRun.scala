package krista

import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.krista.SimpleScenario
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class TestRun extends Simulation {

		val httpProtocol = http
    //.proxy(Proxy("127.0.0.1",9999)) // uncomment this to route traffic to fiddler
    .baseUrl("http://localhost:8080/") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0")

		// Количество виртуальных пользователей - размер пула потоков
		// можно задавать с запасом, это неточное значение, а значение большее, чем нужно
		// должно быть больше, чем Protocol.cfg.tps() * pase_sec, где pase_sec - размер шага нагрузки
		val virtual_users_count : Int = 200

		// Количество запросов в одной итерации
		val userOpenMainPage_Requests = 7

		val maxTPS = 5
		val maxRPS = (maxTPS * userOpenMainPage_Requests ).toInt
		// Длительность теста
		val duration_sec = 60

		val userOpenMainPage : ScenarioBuilder =
			scenario("userRequestPage")
				.forever(
					SimpleScenario.simpleScenario()
				)

		setUp(
			userOpenMainPage
			.inject(
				rampConcurrentUsers(0) to (virtual_users_count) during (duration_sec)
			)
			.protocols(httpProtocol)
			.throttle(
				reachRps(maxRPS) in (duration_sec)
			)
		)


/*
		setUp(
			userOpenMainPage
				.inject(
					constantConcurrentUsers(94) during (duration_sec * 2)
				)
				.protocols(httpProtocol)
				.throttle(
					reachRps(maxRPS) in (duration_sec * 2)
				)
		)

 */
}