package com.ejemplo.crudpersona;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Las pruebas usan su propia base de datos para no tocar los datos reales
@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/test.db")
class CrudPersonaApplicationTests {

    @Test
    void contextLoads() {
    }
}
