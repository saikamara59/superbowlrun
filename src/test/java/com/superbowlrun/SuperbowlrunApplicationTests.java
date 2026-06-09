package com.superbowlrun;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // keeps the interactive DraftGameRunner from launching during the test
class SuperbowlrunApplicationTests {

	@Test
	void contextLoads() {
	}

}
