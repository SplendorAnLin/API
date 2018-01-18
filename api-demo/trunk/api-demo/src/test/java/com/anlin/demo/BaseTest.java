package com.anlin.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 测试类的父类，自动加载spring配置文件且运行于事务之下，测试完毕之后不回滚
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("development")
public class BaseTest {

	@Test
	public void testConfig() {

	}
}