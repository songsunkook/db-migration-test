package io.spring.dbmigration;

import static org.assertj.core.api.Assertions.assertThat;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DbMigrationApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void 애플리케이션_정상_구동() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health", 
            String.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    void 사용자_생성_조회_수정_동작() throws JSONException {
        // 사용자 생성
        String createUserJson = """
            {
                "fullName": "홍길동",
                "email": "hong@test.com"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(createUserJson, headers);

        ResponseEntity<String> createResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/users", 
            request, 
            String.class
        );

        assertThat(createResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String expectedCreateJson = """
            {
                "id": 1,
                "fullName": "홍길동",
                "email": "hong@test.com"
            }
            """;
        JSONAssert.assertEquals(expectedCreateJson, createResponse.getBody(), JSONCompareMode.LENIENT);

        // 사용자 목록 조회
        ResponseEntity<String> listResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/users", 
            String.class
        );

        assertThat(listResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String expectedListJson = """
            [
                {
                    "id": 1,
                    "fullName": "홍길동",
                    "email": "hong@test.com"
                }
            ]
            """;
        JSONAssert.assertEquals(expectedListJson, listResponse.getBody(), JSONCompareMode.LENIENT);

        // 특정 사용자 조회
        ResponseEntity<String> userResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/users/1", 
            String.class
        );

        assertThat(userResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String expectedUserJson = """
            {
                "id": 1,
                "fullName": "홍길동",
                "email": "hong@test.com"
            }
            """;
        JSONAssert.assertEquals(expectedUserJson, userResponse.getBody(), JSONCompareMode.LENIENT);

        // 표시명 조회
        ResponseEntity<String> displayNameResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/users/1/display-name", 
            String.class
        );

        assertThat(displayNameResponse.getStatusCode().is2xxSuccessful()).isTrue();
        String expectedDisplayNameJson = """
            {
                "fullName": "홍길동"
            }
            """;
        JSONAssert.assertEquals(expectedDisplayNameJson, displayNameResponse.getBody(), JSONCompareMode.STRICT);
    }
}
