package msl.test;


//import org.apache.log4j.Logger;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class LoggingDemo {

	// private Logger log = Logger.getLogger(LoggingDemo.class);
	
	/**
	 * log测试
	 */
	@Test
	public void run1() {
		//Logger log = Logger.getLogger(LoggingDemo.class);
		//log.info("run1执行了");
		//log.debug("debug");
	}
	
	/**
	 * 测试slf4j-api、和slf4j-log4j12
	 */
	@Test
	public void run2() {
		org.slf4j.Logger logger = LoggerFactory.getLogger(LoggingDemo.class);
		int a = 1;
		int b = 2;
		logger.info("输出a的值{},b的值是{}", a,b);
		logger.debug("debug");
	}
}
