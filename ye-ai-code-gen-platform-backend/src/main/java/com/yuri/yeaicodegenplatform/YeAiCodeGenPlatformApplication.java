package com.yuri.yeaicodegenplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yuri.yeaicodegenplatform.mapper")
public class YeAiCodeGenPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(YeAiCodeGenPlatformApplication.class, args);
	}

}
