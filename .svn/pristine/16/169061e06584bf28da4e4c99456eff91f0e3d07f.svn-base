package msl.test;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

import lombok.Data;
import msl.utils.JsonUtils;

public class JsonUtilsTest {

	/**
	 * 封装target信息
	 */
	@Data
	public static class Target{
		private String address = null;
		private String description = null;
		private String criticality = "10";
		
		private String target_id;
		private String type = "default";
	}
	
	@Test
	public void run() {

//		Target target = new JsonUtilsTest().new Target();
//		target.setAddress("address");
//		target.setDescription("description");
//		//target.setCriticality("criticality");
//		
//		String targetJson = JsonUtils.toJSONString(target, "address", "description", "criticality");
//		System.out.println(targetJson);
	}
	
	
	@Test
	public void run1() {
		
		String jsonString = "{\r\n" + 
				"	 \"address\": \"https://www.zto.com/\",\r\n" + 
				"	 \"criticality\": 10,\r\n" + 
				"	 \"description\": \"\",\r\n" + 
				"	 \"type\": \"default\",\r\n" + 
				"	 \"target_id\": \"39fb8ac9-9171-4b18-8235-973b1edff1fc\"\r\n" + 
				"	}";
		
		Target parseObject = JSON.parseObject(jsonString, Target.class);
		System.out.println(parseObject.getCriticality());
	}
		
		

}
