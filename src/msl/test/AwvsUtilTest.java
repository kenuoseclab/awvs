package msl.test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import msl.utils.AwvsUtils;
import msl.utils.AwvsUtils.Auth;

public class AwvsUtilTest {

	/**
	 * 测试时间格式转换
	 * @throws ParseException 
	 */
	@Test
	public void run0() throws ParseException {
		String dd = "2020-05-15 23:25:00";
		SimpleDateFormat sdf = new SimpleDateFormat();
		String string2Date = "yyyy-MM-dd HH:mm:ss";
		String date2String = "yyyyMMdd'T'HHmmssZZZZZ";
		sdf.applyPattern(string2Date);
		Date date = sdf.parse(dd);
		sdf.applyPattern(date2String);
		String string = sdf.format(date);
		
		 System.out.println(string);
	}
	
	/**
	 *测试批量添加，按照文件行设置，待优化,超过2个就会卡死
	 * @throws IOException 
	 */
	@Test
	public void run1() throws IOException {
		Auth auth = AwvsUtils.login("614236065@qq.com", "password", "127.0.0.1");
		List<String> scans = AwvsUtils.addScans(new File("src/files/a.txt"), auth);
		System.out.println("-----------------扫描成功清单--------------------");
		for (String string : scans) {
			System.out.println(string);
		}
	}
	
}
