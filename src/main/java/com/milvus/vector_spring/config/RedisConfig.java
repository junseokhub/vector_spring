package com.milvus.vector_spring.config;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DnsResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.master.nodes}")
    private String masterNodes;

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${redis.nodes.mapping}")
    private String mappingStr;

    private Map<String, String> getIpMapping() {
        Map<String, String> map = new HashMap<>();
        if (mappingStr == null || mappingStr.isEmpty()) return map;

        String[] pairs = mappingStr.split(",");
        for (String pair : pairs) {
            String[] kv = pair.trim().split(":");
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // 1. 초기 접속을 위한 Service IP (기존 유지)
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(
                List.of(masterNodes)
        );
        redisClusterConfiguration.setPassword(password);

        Map<String, String> ipMapping = getIpMapping();
        DnsResolver dnsResolver = host -> {
            if (ipMapping.containsKey(host)) {
                String target = ipMapping.get(host);
                log.debug("Custom DNS Resolve: {} -> {}", host, target);
                return InetAddress.getAllByName(target);
            }
            return DnsResolver.jvmDefault().resolve(host);
        };

        ClientResources resources = DefaultClientResources.builder()
                .dnsResolver(dnsResolver)
                .build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientResources(resources)
                .build();

        return new LettuceConnectionFactory(redisClusterConfiguration, clientConfig);
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
