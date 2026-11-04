package dev.erick.multitenancy.integration;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(value = IntegrationTestResource.class, restrictToAnnotatedClass = true)
class TenantIsolationIT {
    @Test
    void johnLogsIntoTenantA() {
        login("johndoe@demo.local", "tenant_a");
    }

    @Test
    void janeLogsIntoTenantB() {
        login("janedoe@demo.local", "tenant_b");
    }

    @Test
    void eachTenantReadsOnlyItsOwnCustomers() {
        String johnToken = login("johndoe@demo.local", "tenant_a");
        String janeToken = login("janedoe@demo.local", "tenant_b");

        given().auth().oauth2(johnToken)
                .when().get("/customers")
                .then().statusCode(200)
                .body("name", hasItems("Acme Customer A", "Acme Customer B"))
                .body("name", not(hasItem("Globex Customer A")))
                .body("name", not(hasItem("Globex Customer B")));

        given().auth().oauth2(janeToken)
                .when().get("/customers")
                .then().statusCode(200)
                .body("name", hasItems("Globex Customer A", "Globex Customer B"))
                .body("name", not(hasItem("Acme Customer A")))
                .body("name", not(hasItem("Acme Customer B")));
    }

    @Test
    void customerCreatedInTenantAIsInvisibleToTenantB() {
        String johnToken = login("johndoe@demo.local", "tenant_a");
        String janeToken = login("janedoe@demo.local", "tenant_b");
        String name = "Private Customer A " + UUID.randomUUID();

        given().auth().oauth2(johnToken)
                .contentType(ContentType.JSON)
                .body(Map.of("name", name))
                .when().post("/customers")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(name));

        given().auth().oauth2(johnToken)
                .when().get("/customers")
                .then().statusCode(200)
                .body("name", hasItem(name));

        given().auth().oauth2(janeToken)
                .when().get("/customers")
                .then().statusCode(200)
                .body("name", not(hasItem(name)));
    }

    @Test
    void customersRequireAuthentication() {
        given().when().get("/customers")
                .then().statusCode(401);
    }

    private String login(String email, String tenant) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", "password"))
                .when().post("/auth/login")
                .then().statusCode(200)
                .body("tenant", equalTo(tenant))
                .body("accessToken", not(blankOrNullString()))
                .extract().path("accessToken");
    }
}
