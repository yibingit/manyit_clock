package y.auto.main;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.google.gson.Gson;

import y.auto.entity.ClockInfo;
import y.auto.entity.UserInfo;
import y.auto.job.Job;
import y.auto.util.Config;
import y.auto.util.CookieManager;
import y.auto.util.HttpUtil;

public class Main {
	
	public static String basePath = Config.getInstance().getConfig("base-path") + "";
	
	public static String url = basePath + "Login.do?_funccode_=C_Login";
	
	public static CookieManager cookieManager;
	public static Map<String, Object> requestPropertys;
	
	/**
	 * ��¼
	 * @param name
	 * @param pass
	 * @return
	 * @throws Exception 
	 */
	public static boolean Login(String name,String pass) throws Exception {
		if(StringUtils.isBlank(name) || StringUtils.isBlank(pass)) {
			return false;
		}
		String params = "name=" + name + "&pass=" + pass + "&async=true";
		requestPropertys = new HashMap<String, Object>();
		cookieManager = new CookieManager();
		URL _url = new URL(basePath);
		// getting cookies: 
		URLConnection conn = _url.openConnection();
		conn.connect(); 
		cookieManager.storeCookies(conn);//�ȴ洢cookie,Ȼ����������������cookie
		String ret = HttpUtil.send("post", url, params, requestPropertys, HttpUtil.ENC_UTF_8, cookieManager);
		if(StringUtils.isBlank(ret)){
			return false;
		}
		Gson gson = new Gson();
		Map<String,Object> info = gson.fromJson(ret, Map.class);
		if(null == info && info.size() <=0) {
			return false;
		}
		if(!success(info)) {
			return false;
		}
		UserInfo.userName = name;
		UserInfo.password = pass;
		return true;
	}
	
	public static String obj2Str(Object o) {
		if(null == o) {
			return "";
		}else {
			return o.toString();
		}
	}
	
	/**
	 * ��ȡ���ڼ�¼
	 */
	public static ClockInfo getUserApplyNo() {
		System.out.println("��ʼ��ȡ���ڼ�¼..");
		String params = "action=getWorkSetTimeData&_page_request_=1&_records_perpage_=999";
		String url = basePath + "AsyncAction.do?_funccode_=C_KQ_GetUserApplyNo";
		String ret = HttpUtil.send("get", url, params, requestPropertys, HttpUtil.ENC_UTF_8, cookieManager);
		Gson gson = new Gson();
		Map<String,Object> info = gson.fromJson(ret, Map.class);
		if(null == info && info.size() <=0) {
			return null;
		}
		if(!success(info)) {
			return null;
		}
		info = (Map<String, Object>) info.get("dataMap");
		ClockInfo clockInfo = new ClockInfo();
		clockInfo.setAmTime(obj2Str(info.get("AMTIME")));
		clockInfo.setPmTime(obj2Str(info.get("PMTIME")));
		clockInfo.setKqStartTime(obj2Str(info.get("KQ_STARTTIME")));
		clockInfo.setKqEndTime(obj2Str(info.get("KQ_ENDTIME")));
		System.out.println("��ȡ�����" + clockInfo.toString());
		return clockInfo;
	}
	
	/**
	 * 1. ��¼
	 * 2. ��ѯ���ڼ�¼
	 * 3. ����Ƿ�δ��
	 * 4. �˳�
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(new InputStreamReader(System.in));
		String name = "";
		do {
			System.out.print("Username: ");
			name = scanner.nextLine();
		}while(StringUtils.isBlank(name));
		String pass = "";
		do {
			System.out.print("Password: ");
			pass = scanner.nextLine();	
		}while(StringUtils.isBlank(pass));
		System.out.print("URL: ");
		//��¼
		if(!Login(name,pass)) {
			System.out.println("��¼ʧ��");
			return;
		}
		System.out.println("��¼�ɹ�����֤�û�������ͨ����..");
//		postClock();
		//[0 * * * * ?] ÿ1���Ӵ���һ��
		String quartzCon = Config.getInstance().getConfig("quartz-con") + "";
		runJob(quartzCon);//[]��ÿ������8�㵽8:55�ڼ������6�㵽6:55�ڼ��ÿ1���Ӵ���
	}
	
	/**
	 * ִ�д�
	 * @param clock
	 */
	public static void executeClock(ClockInfo clock) {
		System.out.println("�Զ��򿨿�ʼ..");
		boolean ret = false;
		String a = StringUtils.isBlank(clock.getKqStartTime()) ? "δǩ��": clock.getKqStartTime();
		String b = StringUtils.isBlank(clock.getKqEndTime()) ? "δǩ��": clock.getKqEndTime();
		System.out.println("ǩ��ʱ�䣺" + a);
		System.out.println("ǩ��ʱ�䣺" + b);
		ret = postClock();
		System.out.println("�Զ��򿨽���("+ret+")..");
	}
	
	public static String getNow() {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	
	public static String getNowDate(String format) {
		if(org.apache.commons.lang.StringUtils.isBlank(format)) {
			format = "YYYY-MM-dd";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date());
	}
	
	public static String getNowMinutes() {
		SimpleDateFormat sdf = new SimpleDateFormat("mm");
		return sdf.format(new Date());
	}
	
	/**
	 * �˳�
	 */
	public static void logout() {
		System.out.println("ִ���˳�...");
		String url = basePath + "Logout.do?_funccode_=C_Logout&async=true";
		String ret = HttpUtil.send("get", url, "", requestPropertys, HttpUtil.ENC_UTF_8, cookieManager);
		System.out.println("�˳����أ�" + ret);
	}
	
	/**
	 * �жϽӿڵĵ����Ƿ�ɹ�
	 * @param info
	 * @return
	 */
	public static boolean success(Map<String,Object> info) {
		if(null == info && info.size() <=0) {
			return false;
		}else {
			System.out.println("�ӿڷ�����Ϣ��" + info);
			String code = obj2Str(info.get("_returncode_"));
			if(StringUtils.isBlank(code)) {
				code = obj2Str(info.get("errcode"));
			}
			return "0".equals(code) || "0.0".equals(code) || info.containsKey("LOGINNAME");
		}
	}
	
	/**
	 * (1-60ȡ�����)
	 * @return
	 */
	public static int getRandom(int condition) {
		Random random = new Random();
		int num = random.nextInt(60);
		if(num < condition) {
			return getRandom(condition);
		}
		return num;
	}
	

	/**
	 * ��������ִ��ǩ����ǩ��
	 * @return
	 */
	public static boolean postClock() {
		try {
			System.out.println("��������ִ�д�,��ǰʱ��" + getNow());
			String url = basePath + "mobile/kq/mobilesignwork";
			String address = Config.getInstance().getConfig("address") + "";
			System.out.println(address);
			String longitude = Config.getInstance().getConfig("longitude") + "";
			String latitude = Config.getInstance().getConfig("latitude") + "";
			String machine_key = Config.getInstance().getConfig("machine_key") + "";
			String addr = address;
			String param = "mobileinfo={\"type\":\"gps\",\"longitude\":\""+longitude+"\",\"latitude\":\""+latitude+"\",\"address\":\""+addr+"\",\"machine_key\":\""+machine_key+"\"}";
			String userAgent = Config.getInstance().getConfig("user-agent") + "";
			requestPropertys.put("user-agent", userAgent);
			String ret = HttpUtil.send("post", url, param, requestPropertys, HttpUtil.ENC_UTF_8, cookieManager);
			Gson gson = new Gson();
			Map<String,Object> info = gson.fromJson(ret, Map.class);
			return success(info);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * ��ʼ��������
	 * @param cron
	 * @throws Exception
	 */
	public static void runJob(String cron1) throws Exception {
		System.out.println("��ʼ��������....");
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

		Scheduler sched = schedFact.getScheduler();

		sched.start();

		// define the job and tie it to our HelloJob class
		JobDetail job1 = JobBuilder.newJob(Job.class).withIdentity("myJob1", "group1").build();
		
		// Trigger the job to run now, and then every 40 seconds
		Trigger trigger1 = TriggerBuilder.newTrigger()
				.withIdentity("myTrigger1", "group1")
				.startNow()
				.withSchedule(CronScheduleBuilder.cronSchedule(cron1)).build();
		
		// Tell quartz to schedule the job using our trigger
		sched.scheduleJob(job1, trigger1);
	}
	
}
