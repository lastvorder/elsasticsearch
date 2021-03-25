package com.last;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class ElasitcsearchApplicationTests {

    @Test
    void contextLoads() {

        log.debug("debug日志");

        log.info("info日志");

        log.warn("warn日志");

        log.error("error日志");
    }

}
