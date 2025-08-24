package com.milvus.vector_spring.config;

import io.lettuce.core.SocketOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Arrays;
@Slf4j
@Configuration
//@EnableRedisHttpSession
public class RedisConfig {
    @Value("${spring.data.redis.cluster.nodes}")
    private String[] clusterNodes;

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${redis.cluster.master.ips}")
    private String[] masterIps;

    @Value("${redis.cluster.slave.ips}")
    private String[] slaveIps;

    @Bean(destroyMethod = "shutdown")
    public ClientResources lettuceClientResources() {
        return DefaultClientResources.builder()
                .dnsResolver(hostname -> {
                    try {
                        String ip = getIpFromEnv(hostname);
                        return new InetAddress[]{InetAddress.getByName(ip)};
                    } catch (Exception e) {
                        throw new RuntimeException("DNS Resolution failed for hostname: " + hostname, e);
                    }
                })
                .build();
    }

    private String getIpFromEnv(String hostname) {
        return switch (hostname) {
            case "redis-leader-0" -> masterIps[0].split(":")[0];
            case "redis-leader-1" -> masterIps[1].split(":")[0];
            case "redis-leader-2" -> masterIps[2].split(":")[0];
            case "redis-follower-0" -> slaveIps[0].split(":")[0];
            case "redis-follower-1" -> slaveIps[1].split(":")[0];
            case "redis-follower-2" -> slaveIps[2].split(":")[0];
            default -> hostname; };
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(100L))
                .keepAlive(true)
                .build();

        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(Arrays.asList(clusterNodes));
        clusterConfig.setPassword(password);

        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .dynamicRefreshSources(true)
                .enableAllAdaptiveRefreshTriggers()
                .enablePeriodicRefresh(Duration.ofSeconds(30))
                .build();

        ClusterClientOptions clientOptions = ClusterClientOptions.builder()
                .pingBeforeActivateConnection(true)
                .autoReconnect(true)
                .socketOptions(socketOptions)
                .topologyRefreshOptions(topologyRefreshOptions)
                .maxRedirects(3).build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientResources(clientResources)
                .clientOptions(clientOptions)
                .build();

        return new LettuceConnectionFactory(clusterConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }



//    @Bean
//    public RedisSessionRepository sessionRepository(RedisConnectionFactory redisConnectionFactory) {
//        return new RedisSessionRepository(redisConnectionFactory);
//    }
}
