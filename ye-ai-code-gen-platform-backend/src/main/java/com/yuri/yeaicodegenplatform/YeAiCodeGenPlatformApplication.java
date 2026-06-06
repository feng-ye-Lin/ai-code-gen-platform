package com.yuri.yeaicodegenplatform;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.yuri.yeaicodegenplatform.mapper")
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
public class YeAiCodeGenPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(YeAiCodeGenPlatformApplication.class, args);
	}

}
