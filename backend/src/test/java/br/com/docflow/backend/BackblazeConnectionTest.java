package br.com.docflow.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "b2.integration", matches = "true")

@SpringBootTest
@ActiveProfiles("local")
class BackblazeConnectionTest {

    @Autowired
    private S3Client s3Client;

    @Value("${b2.bucket-name}")
    private String bucketName;

    @Test
    void deveAcessarBucketBackblaze() {
        assertDoesNotThrow(() ->
                s3Client.headBucket(
                        HeadBucketRequest.builder()
                                .bucket(bucketName)
                                .build()
                )
        );
    }
}