package com.croman.singlevendorecommerce.thumbnail;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbnailJobPublisher {

	private final RedisTemplate<String, String> redisTemplate;


    public void publishJob(String productId, String imageId, String imageUrl) {
        ObjectNode job = JsonNodeFactory.instance.objectNode();
        job.put("image_url", imageUrl);
        job.putArray("sizes").add(200).add(400);
        job.put("timestamp", System.currentTimeMillis() / 1000);
        redisTemplate.opsForList().leftPush("thumbnail_jobs", job.toString());
        log.debug("Job published to Redis: {}", job.toString());

    }
}
