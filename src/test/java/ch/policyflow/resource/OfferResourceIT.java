package ch.policyflow.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@code /api/offers} exercising the full HTTP stack
 * (routing, JSON (de)serialisation, validation, security and persistence) via
 * RestAssured against the Dev Services database.
 */
@QuarkusTest
class OfferResourceIT {

    @Test
    @DisplayName("Unauthenticated request is rejected with 401")
    void unauthenticatedReturns401() {
        given()
                .when().get("/api/offers")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "advisor", roles = {"ADVISOR"})
    @DisplayName("GET /api/offers returns the offer list")
    void listReturnsOffers() {
        given()
                .when().get("/api/offers")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(user = "advisor", roles = {"ADVISOR"})
    @DisplayName("POST /api/offers creates an offer with a calculated premium")
    void createReturns201WithPremium() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "customerId": 1,
                          "providerId": 1,
                          "canton": "LU",
                          "age": 32,
                          "franchise": 1000,
                          "unfalleinschluss": false,
                          "notes": "IT created offer"
                        }
                        """)
                .when().post("/api/offers")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("monthlyPremium", greaterThan(0f))
                .body("status", org.hamcrest.Matchers.equalTo("PENDING"));
    }

    @Test
    @TestSecurity(user = "advisor", roles = {"ADVISOR"})
    @DisplayName("POST with an invalid franchise yields 400")
    void invalidFranchiseReturns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "customerId": 1,
                          "providerId": 1,
                          "canton": "LU",
                          "age": 32,
                          "franchise": 999,
                          "unfalleinschluss": false
                        }
                        """)
                .when().post("/api/offers")
                .then().statusCode(400)
                .body("status", org.hamcrest.Matchers.equalTo(400));
    }

    @Test
    @TestSecurity(user = "advisor", roles = {"ADVISOR"})
    @DisplayName("POST with a missing required field yields 400 with a violation message")
    void missingFieldReturns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "providerId": 1,
                          "canton": "LU",
                          "age": 32,
                          "franchise": 1000
                        }
                        """)
                .when().post("/api/offers")
                .then().statusCode(400)
                .body("error", org.hamcrest.Matchers.equalTo("Validation failed"))
                .body("violations.size()", greaterThan(0));
    }

    @Test
    @TestSecurity(user = "advisor", roles = {"ADVISOR"})
    @DisplayName("Accepting a freshly created offer issues a policy")
    void acceptOfferCreatesPolicy() {
        // Create a dedicated offer so the test is independent of seed state.
        Integer offerId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "customerId": 5,
                          "providerId": 2,
                          "canton": "GE",
                          "age": 40,
                          "franchise": 500,
                          "unfalleinschluss": true
                        }
                        """)
                .when().post("/api/offers")
                .then().statusCode(201)
                .extract().path("id");

        given()
                .when().put("/api/offers/" + offerId + "/accept")
                .then().statusCode(200)
                .body("policyNumber", notNullValue())
                .body("status", org.hamcrest.Matchers.equalTo("ACTIVE"));
    }
}
