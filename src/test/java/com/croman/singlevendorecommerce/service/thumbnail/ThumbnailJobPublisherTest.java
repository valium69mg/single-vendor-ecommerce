package com.croman.singlevendorecommerce.service.thumbnail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.croman.singlevendorecommerce.service.thumbnail.ThumbnailJobPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class ThumbnailJobPublisherTest {

    private static final String REDIS_QUEUE_KEY = "thumbnail_jobs";
    private static final String IMAGE_URL = "https://cdn.example.com/image.png";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @InjectMocks
    private ThumbnailJobPublisher publisher;

    // -------------------------
    // Helpers
    // -------------------------

    private void stubRedisListOps() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    private JsonNode parseJson(String json) throws Exception {
        return objectMapper.readTree(json);
    }

    // -------------------------
    // Tests
    // -------------------------

    @Test
    void testPublishJobPushesThumbnailJobToRedisWithExpectedPayload() throws Exception {
        // Arrange
        stubRedisListOps();
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        publisher.publishJob(IMAGE_URL);

        // Assert
        verify(listOperations).leftPush(
                org.mockito.ArgumentMatchers.eq(REDIS_QUEUE_KEY),
                payloadCaptor.capture()
        );

        JsonNode job = parseJson(payloadCaptor.getValue());

        assertThat(job.get("image_url").asText()).isEqualTo(IMAGE_URL);
        assertThat(job.get("sizes")).hasSize(2);
        assertThat(job.get("sizes").get(0).asInt()).isEqualTo(200);
        assertThat(job.get("sizes").get(1).asInt()).isEqualTo(400);
        assertThat(job.get("timestamp").asLong()).isPositive();
    }
}