package y.auto.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;

public class HttpUtil{
	
	public static final String POST = "post";
	
	public static final String GET = "get";
	
	public static final String ENC_UTF_8 = "UTF-8";
	
	public static final String ENC_GBK = "GBK";

	private static void trustAllHttpsCertificates() throws Exception {
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new miTM();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	/**
	 * ��ָ��URL����GET���������� 
	 * @param url		���������URL
	 * @param params	����������������Ӧ����name1=value1&name2=value2����ʽ��
	 * @param encode	���뷽ʽ����ֹ���룩
	 * @return			URL������Զ����Դ����Ӧ
	 */
	public static String sendGet(String url, String params,String encode){
		return send(GET, url, params, null, encode, null);
		 
	}

	/**
	 * ��ָ��URL����POST����������
	 * @param url ���������URL
	 * @param params
	 * @param encode	���뷽ʽ����ֹ���룩
	 * @return URL������Զ����Դ����Ӧ
	 */
	public static String sendPost(String url, String params,String encode){
		return send(POST, url, params, null, encode, null);
	}
	
	/**
	 * ��ָ��URL����POST���������� UTF-8
	 * @param url
	 * @param params
	 * @return
	 */
	public static String sendPost(String url, String params){
		return send(POST, url, params, null, HttpUtil.ENC_UTF_8, null);
	}
	
	/**
	 * ��ָ����URL����POST����
	 * @param url				ָ����URL
	 * @param params			����
	 * @param requestPropertys	����ͷ
	 * @param encode			���뷽ʽ
	 * @return
	 */
	public static String sendPost(String url, String params, Map<String,Object> requestPropertys,String encode){
		return send(POST, url, params, requestPropertys, encode, null);
	}

	public static String sendPost(String url, Map params){
		return sendPost(url, params, null, HttpUtil.ENC_UTF_8);
	}

	public static String sendPost(String url, Map params,String encode){
		return sendPost(url, params, null, encode);
	}

	public static String sendPost(String url, Map params, Map<String,Object> requestPropertys,String encode){
		StringBuilder paramStr = new StringBuilder();
		if(null != params){
			Set<String> keys = params.keySet();
			for(String key : keys){
				if(paramStr.length() != 0){
					paramStr.append("&");
				}
				paramStr.append(key).append("=").append(params.get(key));
			}
		}
		return send(POST, url, paramStr.toString(), requestPropertys, encode, null);
	}
			
	/**
	 * ����Http����
	 * @param method			����ķ���
	 * @param url				�����URL
	 * @param params			����Ĳ���
	 * @param requestPropertys	����ͷMap
	 * @param encode			���뷽ʽ����ֹ���룩
	 * @param cookieManager		cookie����
	 * @return
	 */
	public static String send(String method, String url, String params, Map<String,Object> requestPropertys,String encode,CookieManager cookieManager) {
		StringBuilder result = new StringBuilder();
		BufferedReader in = null;
		OutputStreamWriter out = null;
		try{
			if(method.equalsIgnoreCase(GET) && StringUtils.isNotBlank(params)){
				if(url.contains("?")){
					url += "&" + params;
				} else{
					url += "?" + params;
				}
			}
			if(url.toLowerCase().startsWith("https")){
				trustAllHttpsCertificates();
				HostnameVerifier hv = new HostnameVerifier() {
					public boolean verify(String urlHostName, SSLSession session) {
						return true;
					}
				};
				HttpsURLConnection.setDefaultHostnameVerifier(hv);
			}

			URL realUrl = new URL(url);
			if(StringUtils.isBlank(encode)){
				encode = HttpUtil.ENC_UTF_8;
			}
			
			// �򿪺�URL֮�������
			URLConnection conn = realUrl.openConnection();
			// ����ͨ�õ���������
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + encode);
			conn.setRequestProperty("Accept-Charset", encode);
			// ����ͨ�õ��Զ�������
			if(requestPropertys != null && !requestPropertys.isEmpty()){
				Iterator<String> it = requestPropertys.keySet().iterator();
				String key;
				String value;
				while(it.hasNext()){
					key = it.next();
					value = requestPropertys.get(key) + "";
					conn.setRequestProperty(key, value);
				}
			}
			//����Cookie
			if(cookieManager != null && cookieManager instanceof CookieManager){
				cookieManager.setCookies(conn);
			}
			if(method.equalsIgnoreCase(GET)){		//����Ƿ���GET����
				// ����ʵ�ʵ�����
				conn.connect();
				// ����BufferedReader����������ȡURL����Ӧ
				InputStream is = conn.getInputStream();
				in = new BufferedReader(new InputStreamReader(is, encode));
			}else if(method.equalsIgnoreCase(POST)){//����Ƿ���POST����
				// ����POST�������������������
				conn.setDoOutput(true);
				conn.setConnectTimeout(10000);
				conn.setDoInput(true);
				// ��ȡURLConnection�����Ӧ�������
				out = new OutputStreamWriter(conn.getOutputStream(), encode); 
				// �����������
				out.write(params);
				// flush������Ļ���
				out.flush();
				// ����BufferedReader����������ȡURL����Ӧ
				InputStream is = conn.getInputStream();
				in = new BufferedReader(new InputStreamReader(is,encode));
			}
			String line;
			while ((line = in.readLine()) != null){
				result.append("\n" ).append(line);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(out != null){
					out.close();
				}
				if(in != null){
					in.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}	
		return result.toString();
	}

	public static String sendPost(String url,Map<String,Object> params,byte[] data){
		StringBuilder result = new StringBuilder();
		PrintWriter out = null;
		BufferedReader in = null;
		try{
			String url2 = url;
			if(params != null && params.size() > 0){
				boolean ishas = false;
				if(url.contains("?")){
					ishas = true;
				}
				if(ishas == false){
					url2 +="?";
				}
				Object[] keys = params.keySet().toArray();
				for(int i=0;i<keys.length;i++){
					String key = "" + keys[i];
					if(ishas == true){
						url2 +="&";
					}
					ishas = true;
					url2 += key +"="+params.get(key);
				}
			}
			URL realUrl = new URL(url2);
			// �򿪺�URL֮�������
			HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
			conn.setRequestMethod("POST");
			// ����POST�������������������
			conn.setDoOutput(true);
			conn.setConnectTimeout(10000);
			conn.setDoInput(true);
			conn.connect();
			if(data != null ){
				conn.getOutputStream().write(data);
				conn.getOutputStream().flush();
				conn.getOutputStream().close();
			}
			// ����BufferedReader����������ȡURL����Ӧ
			InputStream is = conn.getInputStream();
			in = new BufferedReader(new InputStreamReader(is,"gb2312"));
			String line;
			while ((line = in.readLine()) != null){
				result.append("\n" ).append(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(out != null){
					out.close();
				}
				if(in != null){
					in.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return result.toString();
	}

	static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}

}
