package com.finale;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Disattivato per evitare il controllo del DB in fase di build locale")
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
