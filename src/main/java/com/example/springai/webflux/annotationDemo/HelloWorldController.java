package com.example.springai.webflux.annotationDemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 注解式 webflux 的示例
 */
@RestController
public class HelloWorldController {
	@GetMapping("/anno/hello")
	public Mono<String> helloWorld() {
		return Mono.just("注解式响应的例子");
	}
}
