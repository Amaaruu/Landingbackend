package Landing.Backend.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class BackendLoadSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    private final ScenarioBuilder healthScenario = scenario("Health Check")
        .exec(
            http("GET /health")
                .get("/api/v1/health")
                .check(status().is(200))
        );

    private final ScenarioBuilder plansScenario = scenario("Consulta Planes")
        .exec(
            http("GET /plans")
                .get("/api/v1/plans")
                .check(status().is(200))
        );

    private final ScenarioBuilder loginScenario = scenario("Login")
        .exec(
            http("POST /auth/login")
                .post("/api/v1/auth/login")
                .body(StringBody("""
                    {"email":"loadtest@test.com","password":"LoadTest123!"}
                    """))
                .check(status().in(200, 401))
        );

    {
        setUp(
            healthScenario.injectOpen(
                rampUsers(10).during(30)
            ),
            plansScenario.injectOpen(
                constantUsersPerSec(5).during(60)
            ),
            loginScenario.injectOpen(
                rampUsers(50).during(10)
            )
        )
        .protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile3().lt(2000),
            global().responseTime().mean().lt(500),
            global().successfulRequests().percent().gt(95.0)
        );
    }
}