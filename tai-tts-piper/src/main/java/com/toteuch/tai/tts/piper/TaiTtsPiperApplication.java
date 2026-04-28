// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.tts.piper;

import com.toteuch.tai.tts.piper.config.TtsPiperProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(TtsPiperProperties.class)
public class TaiTtsPiperApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaiTtsPiperApplication.class, args);
    }
}
