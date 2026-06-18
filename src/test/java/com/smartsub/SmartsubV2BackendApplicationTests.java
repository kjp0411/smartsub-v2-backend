package com.smartsub;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("테스트 환경 DB 설정 필요 - 추후 H2 또는 TestContainers로 개선 예정")
class SmartsubV2BackendApplicationTests {
	@Test
	void contextLoads() {
	}
}