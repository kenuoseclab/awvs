package msl.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;
import msl.test.AwvsConstant;

/**
 * awvs的常用工具类，适用于awvs_vsersion_12
 * 
 * @author 61423
 *
 */
public class AwvsUtils {

	private final static Logger log = LoggerFactory.getLogger(AwvsUtils.class);
	static HttpClient httpClient = null;
	static CookieStore cookieStore = new BasicCookieStore();
	static {
		try {
			httpClient = HttpClients.custom().setConnectionTimeToLive(10, TimeUnit.MINUTES)
					.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
					.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setDefaultCookieStore(cookieStore).build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			log.error("初始化 HttpCient 失败......");
			e.printStackTrace();
		}
	}

	/**
	 * 封装登录信息
	 */
	@Data
	public static class Auth {

		// 用户信息
		private String email = null;
		private String password = null;
		private Boolean remember_me = false;
		private Boolean logout_previous = true;

		// 用户口令
		@JSONField(serialize = false)
		private Header xauth = null;
		@JSONField(serialize = false)
		private Cookie uisCookie = null;

		// 请求参数
		@JSONField(serialize = false)
		private String scheme = "https";
		@JSONField(serialize = false)
		private String ip = "127.0.0.1";
		@JSONField(serialize = false)
		private Integer port = 3443;
		@JSONField(serialize = false)
		private String path = null;
		@JSONField(serialize = false)
		private HttpMethod method = HttpMethod.POST;

		public void setPassword(String password) {
			this.password = EncryptUtils.getSHA256(password);
		}
	}

	/**
	 * 用于获取request
	 * 
	 * @param auth 用于的登录信息
	 * @param json 要发送的json
	 * @return
	 */
	private static HttpResponse getResponse(Auth auth, String json) {
		log.debug("开始初始化uri");
		URI uri = null;
		try {
			uri = new URIBuilder().setScheme(auth.getScheme()).setHost(auth.getIp()).setPort(auth.getPort())
					.setPath(auth.getPath()).build();
		} catch (URISyntaxException e) {
			log.error("uri初始化失败......");
			e.printStackTrace();
		}
		log.debug("uri初始化成功");

		log.debug("开始构造request请求");
		HttpUriRequest request = RequestBuilder.create(auth.getMethod().toString())
				.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON)).addHeader(auth.getXauth()).setUri(uri).build();
		log.debug("request请求构造完成");

		HttpResponse response = null;
		try {
			log.debug("开始访问\t{}", auth.getIp());
			response = httpClient.execute(request);
		} catch (IOException e) {
			log.error("访问\t{}失败......", auth.getIp());
			e.printStackTrace();
		}
		log.debug("来自于{}的访问结果: {}", auth.getIp(), response.getStatusLine());

		return response;
	}

	/**
	 * 从response中获取json
	 * 
	 * @param response
	 * @return
	 */
	private static String getResponseJson(HttpResponse response) {
		InputStream content = null;
		try {
			content = response.getEntity().getContent();
		} catch (UnsupportedOperationException | IOException e) {
			e.printStackTrace();
		}
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(content));
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			log.error("获取response的实体失败......");
			e.printStackTrace();
		}

		return sb.toString();
	}

	/**
	 * 登录模块
	 * 
	 * @param email    用户账号
	 * @param password 用户密码
	 * @param ip       awvs服务器ip
	 * @return
	 */
	public static Auth login(String email, String password, String ip) {
		log.debug("开始封装登录信息");
		Auth auth = new Auth();
		auth.setEmail(email);
		auth.setPassword(password);
		auth.setIp(ip);
		auth.setPath(AwvsConstant.LOGINPATH);
		auth.setMethod(HttpMethod.POST);
		String authJson = JsonUtils.toJSONString(auth);
		log.debug("封装完成...\t {}", authJson);

		log.debug("开始请求awvs");
		HttpResponse response = getResponse(auth, authJson);
		log.debug("请求完成，获取到response");

		if (response.getHeaders("X-Auth").length > 0) {
			auth.setXauth(response.getHeaders("X-Auth")[0]);
			auth.setUisCookie(CookUtils.getCookieByName("ui_session", cookieStore.getCookies()));
			log.info("登录成功:返回X-Auth：{}", auth.getXauth().getValue());
			log.debug("登录成功，返回ui_session：{}", auth.getUisCookie().getValue());
		}

		return auth;
	}

	/**
	 * 封装target信息
	 */
	@Data
	public static class Target {
		private String address = null;
		private String description = null;
		private String criticality = "10";

		private String target_id = null;
		private String type = null;
		private Auth auth = null;

		public void setCriticality(Integer criticality) {
			this.criticality = criticality.toString();
		}
	}

	/**
	 * 添加target模块
	 * 
	 * @param address  目标的请求路径
	 * @param email    用户名
	 * @param password 用户密码
	 * @param ip       awvs服务器ip
	 */
	public static Target addTarget(String address, Auth auth) {

		log.debug("开始更新请求路径");
		auth.setPath(AwvsConstant.TARGETPATH);
		log.debug("开始封装target请求参数");

		Target target = new Target();
		target.setAddress(address);
		String targetJson = JsonUtils.toJSONString(target);
		log.debug("请求参数是：{}", targetJson);

		log.debug("开始请求awvs");
		HttpResponse response = getResponse(auth, targetJson);
		log.debug("请求完成，获取到response");

		log.debug("开始解析获取的的json");
		String responseJson = getResponseJson(response);
		
		if (responseJson.length() != 0) {
			target = JsonUtils.parseObject(responseJson, Target.class);
			log.debug("解析完成");
			log.info("添加目标:{}", target.getAddress());
		}
		return target;
	}

	@Data
	public static class Profile {
		private String profile_id;
		private Schedule schedule = new Schedule();
		private String target_id;
		private String ui_session_id;
		private Target target;
	}

	@Data
	public static class Schedule {
		private Boolean disable;
		private String start_date;
		private Boolean time_sensitive;
		
		public void setStart_date(String dateString) {
			if(dateString!=null && dateString.trim().length()>0) {
				try {
					//String dd = "2020-05-15 23:25:00";
					SimpleDateFormat sdf = new SimpleDateFormat();
					String string2Date = "yyyy-MM-dd HH:mm:ss";
					String date2String = "yyyyMMdd'T'HHmmssZZZZZ";
					sdf.applyPattern(string2Date);
					Date date = sdf.parse(dateString);
					sdf.applyPattern(date2String);
					this.start_date = sdf.format(date);
				}catch (Exception e) {
					log.warn("转换时间格式失败...");
				}
			}else {
				this.start_date = dateString;
			}
			
		}
	}
	

	/**
	 * 添加扫描
	 * 
	 * @param address
	 * @param email
	 * @param password
	 * @param ip
	 * @return
	 */
	public static Profile addScan(Target target, Auth auth) {
		auth.setPath(AwvsConstant.SCANPATH);

		Profile profile = new Profile();
		profile.setProfile_id(AwvsConstant.TYPE_FUllSCAN);
		profile.getSchedule().setDisable(AwvsConstant.SCHEDULE_DISABLE_FALSE);
		profile.getSchedule().setStart_date(null);
		profile.getSchedule().setTime_sensitive(false);
		profile.setTarget_id(target.getTarget_id());
		profile.setUi_session_id(AwvsConstant.UI_SESSION_ID); // 固定值
		String profileJson = JsonUtils.toJSONString(profile);
		log.debug("请求参数是：{}", profileJson);

		HttpResponse response = getResponse(auth, profileJson);

		log.debug("添加扫描结果：{}", response.getStatusLine());

		log.info("添加扫描:{}",target.getAddress());
		return null;
	}

	
	/**
	 * 批量添加扫描任务
	 * @param file
	 * @param auth
	 * @return
	 */
	public static List<String> addScans(File file, Auth auth) {
		List<String> list = new ArrayList<String>();
		Set<String> addresses = null;
		try {
			addresses = FileUtils.getListByLine(file);
			log.debug("获取address完成");
		} catch (IOException e) {
			log.warn("获取address失败...");
			e.printStackTrace();
		}
		
		for (String address : addresses) {
			Target target = addTarget(address, auth);
			addScan(target, auth);
			list.add(address);
		}
		return list;
	}
}
