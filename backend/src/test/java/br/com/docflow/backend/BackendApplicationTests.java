package br.com.docflow.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "b2.endpoint=https://s3.us-east-005.backblazeb2.com",
        "b2.region=us-east-005",
        "b2.bucket-name=docflow-documentos",
        "b2.access-key-id=test-key",
        "b2.secret-access-key=test-secret"
})
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}