package ch.so.agi.ilivalidator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@SpringBootTest
@ActiveProfiles("test")
//@TestPropertySource(properties = { "JDBC_URL=jdbc:sqlite:/tmp/jobrunr_db.sqlite", "login.pwd=k12" })
class IlivalidatorWebServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
