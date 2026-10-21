package com.cine.ms_notificaciones;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "app.mail.enabled=false"
})
class MsNotificacionesApplicationTests {

    @Test
    void contextLoads() {
    }

}
