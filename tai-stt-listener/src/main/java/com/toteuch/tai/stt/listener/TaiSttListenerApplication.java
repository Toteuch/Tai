// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener;

import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SttListenerProperties.class)
public class TaiSttListenerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaiSttListenerApplication.class, args);
    }
}
