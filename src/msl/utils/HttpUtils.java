package msl.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import lombok.Data;


/**
 * @author weihuibin 2019/8/29
 */
public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    public static final String JSON_TYPE = "application/json";
    private static CloseableHttpClient client;
    private static RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setExpectContinueEnabled(false)
            // 禁用重定向
            .setRedirectsEnabled(false)
            .setRelativeRedirectsAllowed(false)
            .build();


    /**
     * 发送post请求，较为全面
     *
     * @param url 接口地址
     * @return
     */
    public static String sendPost(String url, Param param) {
        if (StringUtils.isEmpty(url)) {
            return StringUtils.EMPTY;
        }
        HttpPost post = getHttpPost(url, param);
        return send(url, param, post);
    }
    public static String sendPostXML(String url, String xml) {
        HttpClient client = HttpUtils.getClient();
        HttpPost post = new HttpPost(url);
        try {
            StringEntity entity = new StringEntity(xml);
            System.out.println(xml);
            entity.setContentType(ContentType.APPLICATION_XML.getMimeType());
            post.setEntity(entity);
            HttpResponse execute = client.execute(post);
            InputStream content = execute.getEntity().getContent();
            String data = IOUtils.toString(content, "UTF-8");
            return data;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static String sendJsonPost(String url, Param param) {
        if (StringUtils.isEmpty(url)) {
            return StringUtils.EMPTY;
        }
        HttpPost post = getJsonHttpPost(url, param);
        return send(url, param, post);
    }

    public static String sendJsonPost(String url, Param param, String contentType) {
        if (StringUtils.isEmpty(url)) {
            return StringUtils.EMPTY;
        }
        HttpPost post = getJsonHttpPost(url, param, contentType);
        return send(url, param, post);
    }

    /**
     * HttpPost对象生成器
     *
     * @param url 接口地址
     * @return HttpPost
     */
    public static HttpPost getHttpPost(String url, Param param) {
        HttpPost post = getDefHttpPost(url, param);
        addHeaders(post, param);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        ;
        if (MapUtils.isNotEmpty(param.getParams())) {
            for (String key : param.params.keySet()) {
                entityBuilder.addTextBody(key, param.params.get(key));
            }
        }
        if (param.dataBytes != null && param.dataBytes.length != 0) {
            entityBuilder.addBinaryBody(param.paramsName, param.dataBytes, ContentType.DEFAULT_BINARY, param.fileName);
        }
        post.setEntity(entityBuilder.build());
        return post;
    }


    /**
     * json格式post生成器
     *
     * @param url 接口地址
     * @return
     */
    private static HttpPost getJsonHttpPost(String url, Param param) {

        HttpPost post = getDefHttpPost(url, param);
        param.contentType = ContentType.APPLICATION_JSON.getMimeType();
        addHeaders(post, param);
        if (param.jsonParam != null) {

            StringEntity entity = new StringEntity(JSON.toJSONString(param.jsonParam), Charset.forName(param.charset));
            post.setEntity(entity);
        }
        return post;
    }

    /**
     * json格式post生成器
     *
     * @param url 接口地址
     * @return
     */
    private static HttpPost getJsonHttpPost(String url, Param param, String contentType) {
        HttpPost post = getDefHttpPost(url, param);
        param.contentType = contentType;
        addHeaders(post, param);
        if (param.jsonParam != null) {

            ContentType contentTypeDomain = ContentType.create(param.contentType, Charset.forName(param.charset));
            StringEntity entity = new StringEntity(JSON.toJSONString(param.jsonParam), contentTypeDomain);
            post.setEntity(entity);
        }
        return post;
    }


    /**
     * 生成默认的默认信息
     *
     * @param url 接口地址
     * @return
     */
    private static HttpPost getDefHttpPost(String url, Param params) {
        HttpPost post = new HttpPost(url);
        post.setProtocolVersion(HttpVersion.HTTP_1_1);
        RequestConfig requestConfig = generateRequestConfig(params.getConnectTimeout(), params.getSocketTimeout());
        post.setConfig(requestConfig);
        return post;
    }


    /**
     * 发送详细的GET请求
     *
     * @param url    接口地址
     * @param params 接口参数
     * @return
     */
    public static String sendGet(String url, Param params) {
        if (StringUtils.isEmpty(url)) {
            return StringUtils.EMPTY;
        }

        HttpGet httpGet = getHttpGet(url, params);
        if (httpGet == null) {
            return StringUtils.EMPTY;
        }
        return send(url, params, httpGet);
    }

    /**
     * 用于将数据进行发送并将响应解析为字符串
     *
     * @param url    接口地址
     * @param params 接口参数
     * @return
     */
    private static String send(String url, Param params, HttpRequestBase requestBase) {
        HttpResponse response = null;
        try {
            response = getClient().execute(requestBase);
            if (response == null) {
                logger.error("HttpClientUtil.sendHttpGetRequest error >>>>>> response is null, url:{}, params:{}, connectTimeout:{}, socketTimeout:{}",
                        url, params);
                return StringUtils.EMPTY;
            }
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("HttpClientUtil.sendHttpGetRequest error >>>>>> method failed:{}, url:{}, params:{}, connectTimeout:{}, socketTimeout:{}",
                        response.getStatusLine(), url, params);
                /* 当状态码不是200时，默认返回空字符串 */
                return StringUtils.EMPTY;
            }
            if (StringUtils.isEmpty(params.getCharset())) {
                params.setCharset("utf-8");

            }
            return EntityUtils.toString(response.getEntity(), params.charset);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    // 会自动释放连接
                    EntityUtils.consume(response.getEntity());
                    requestBase.releaseConnection();
                }
            } catch (IOException e) {
                logger.error("HttpClientUtil.sendHttpPostRequest error, stream connection close failure, url:{}, params:{}, connectTimeout:{}, socketTimeout:{}",
                        url, params, e);
            } catch (Exception e) {
                logger.error("HttpClientUtil.sendHttpPostRequest error, url:{}, params:{}, connectTimeout:{}, socketTimeout:{}",
                        url, params, e);
            }
        }
        return StringUtils.EMPTY;
    }


    /**
     * HttpGet对象生成器
     *
     * @param url 接口地址
     * @return HttpGet
     */
    private static HttpGet getHttpGet(String url, Param param) {
        try {
            //生成URLBuilder用于构建url以及参数请求连接
            URIBuilder builder = new URIBuilder(url);
            if (MapUtils.isNotEmpty(param.params)) {
                builder.addParameters(paramsToNameValuePairs(param.params));
            }
            HttpGet httpGet = new HttpGet(builder.build());
            //设置HTTP协议版本
            httpGet.setProtocolVersion(HttpVersion.HTTP_1_1);
            //添加消息头
            addHeaders(httpGet, param);
            //添加请求设施参数
            httpGet.setConfig(generateRequestConfig(param.connectTimeout, param.socketTimeout));
            return httpGet;
        } catch (URISyntaxException e) {
            logger.error("HttpClientUtil.generateHttpGet error, url:{}, params:{}, connectTimeout:{}, socketTimeout:{}",
                    url, param, e);
        }
        return null;
    }

    /**
     * 将Map参数解析为list参数
     *
     * @param params 参数
     * @return
     */
    private static List paramsToNameValuePairs(Map<String, String> params) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        if (MapUtils.isNotEmpty(params)) {
            for (String key : params.keySet()) {
                nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
            }
        }
        return nameValuePairs;
    }

    /**
     * 单例方式获取Client实例
     *
     * @return
     */
    public static HttpClient getClient() {
        if (client == null) {
            synchronized (HttpUtils.class) {
                if (client == null) {
                    client = makeClient();
                }
            }
        }
        return client;
    }

    /**
     * http header
     *
     * @param httpRequest
     */
    private static void addHeaders(HttpRequestBase httpRequest, Param param) {
        if (StringUtils.isNotEmpty(param.contentType)) {
            httpRequest.addHeader("Content-type", param.contentType + "; charset=" + param.charset);
        }

        httpRequest.addHeader("Accept", param.contentType);
        httpRequest.addHeader("encoding", param.charset);

        // 设置HTTP短连接, 在一次请求/响应之后, 就会关闭连接
        httpRequest.addHeader("Connection", "close");
        if (MapUtils.isNotEmpty(param.headers)) {
            for (String key : param.headers.keySet()) {
                httpRequest.addHeader(key, param.headers.get(key));
            }
        }
        if (CollectionUtils.isNotEmpty(param.cookies)) {
            for (String cookie : param.cookies) {
                if (StringUtils.isNotBlank(cookie)) {
                    httpRequest.addHeader("Cookie", cookie);
                }
            }
        }
    }

    /**
     * RequestConfig对象生成器
     *
     * @param connectTimeout 请求超时时间
     * @param socketTimeout  等待数据超时时间
     * @return RequestConfig
     */
    private static RequestConfig generateRequestConfig(int connectTimeout, int socketTimeout) {
        return RequestConfig.copy(defaultRequestConfig)
                // 设置从Connect Manager获取Connection的超时时间, 即连接不够用的时候的等待超时时间, 一定要设置, 而且不能太大
                .setConnectionRequestTimeout(connectTimeout / 2)
                // 设置请求超时时间
                .setConnectTimeout(connectTimeout)
                // 设置等待数据超时时间
                .setSocketTimeout(socketTimeout)
                .build();
    }

    /**
     * 生成cilent
     *
     * @return
     */
    private static CloseableHttpClient makeClient() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .setDefaultRequestConfig(getDefaultRequestConfig())
                .setConnectionManager(getConnectionManager()).build();
        return httpClient;
    }

    /**
     * 获取连接管理器
     *
     * @return
     */
    private static HttpClientConnectionManager getConnectionManager() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        //最大连接数
        manager.setMaxTotal(10);
        //每个路由最大连接数
        manager.setDefaultMaxPerRoute(100);
        //设置到某个路由的最大连接数，会覆盖defaultMaxPerRoute
        //manager.setMaxPerRoute(new HttpRoute(new HttpHost()));
        return manager;
    }

    /**
     * 获取默认连接设置
     *
     * @return
     */
    private static RequestConfig getDefaultRequestConfig() {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setExpectContinueEnabled(false)
                // 禁用重定向
                .setRedirectsEnabled(false)
                .setRelativeRedirectsAllowed(false)
                .build();
        return defaultRequestConfig;
    }

    public static Param getParam(String paramName, String fileName, byte[] bytes) {
        return new Param(paramName, fileName, bytes);
    }

    public static Param getParam() {
        return new Param();
    }

    public static Param getParam(Map<String, String> params, Map<String, String> headers, List<String> cookies, int connectTimeout, int socketTimeout, String charset) {
        return new Param(params, headers, cookies, connectTimeout, socketTimeout, charset);
    }

    public static Param getParam(Map<String, String> params) {
        return new Param(params);
    }

    public static Param getJsonParam(Object params) {
        return new Param(params);
    }

    /**
     * 参数对象，用于封装参数
     */
    @Data
    public static class Param {
        private Map<String, String> params = null;
        private Object jsonParam = null;
        private Map<String, String> headers = null;
        private List<String> cookies = null;
        private String fileName = null;
        private String paramsName = null;
        private byte[] dataBytes = null;
        private File dataFile = null;
        private InputStream dataInput = null;
        private int connectTimeout = 20000;
        private int socketTimeout = 20000;
        private String charset = "UTF-8";
        private String contentType = ContentType.APPLICATION_FORM_URLENCODED.getMimeType();
        ;

        public Param() {

        }


        public Param putParams(String key, String value) {
            params = MapUtils.isEmpty(params) ? new HashMap<>() : params;
            params.put(key, value);
            return this;
        }

        public Param putHeaders(String key, String value) {
            headers = MapUtils.isEmpty(headers) ? new HashMap<>() : headers;
            headers.put(key, value);
            return this;
        }


        public Param(String paramsName, String fileName, byte[] dataBytes) {
            this.fileName = fileName;
            this.paramsName = paramsName;
            this.dataBytes = dataBytes;
        }

        public Param(Map<String, String> params, String fileName, String paramsName, byte[] dataBytes) {
            this.params = params;
            this.fileName = fileName;
            this.paramsName = paramsName;
            this.dataBytes = dataBytes;
        }

        public Param(Object jsonParam, Map<String, String> headers, List<String> cookies) {
            this.jsonParam = jsonParam;
            this.headers = headers;
            this.cookies = cookies;
        }

        public Param(Map<String, String> params, Map<String, String> headers, List<String> cookies) {
            this.params = params;
            this.headers = headers;
            this.cookies = cookies;
        }

        public Param(Map<String, String> params) {
            this.params = params;
        }

        public Param(Object jsonParam) {
            this.jsonParam = jsonParam;
        }

        public Param(Map<String, String> params, Map<String, String> headers, List<String> cookies, int connectTimeout, int socketTimeout, String charset) {
            this.params = params;
            this.headers = headers;
            this.cookies = cookies;
            this.connectTimeout = connectTimeout;
            this.socketTimeout = socketTimeout;
            this.charset = charset;
        }
    }
}
