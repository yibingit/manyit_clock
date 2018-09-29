package y.auto.job;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import y.auto.entity.ClockInfo;
import y.auto.entity.UserInfo;
import y.auto.main.Main;
import y.auto.util.Config;
import y.auto.util.HolidayUtil;

public class Job implements org.quartz.Job{
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("�����˿�ʼִ��...��"+Main.getNow()+"��");
		String nowDate = Main.getNowDate("YYYYMMdd");
		int week = HolidayUtil.isWorkDay(nowDate);
		if(week != 0) {
			System.out.println("���ǹ����գ����򿨡�"+nowDate+"��");	
			return;
		}
		int random = Integer.parseInt(Config.getInstance().getConfig("random") + "");
		String start = Main.getRandom(random) + "";
		String end = Main.getRandom(random) + "";
		if(start.length() < 2) {
			start = "0" + start;
		}
		if(end.length() < 2) {
			end = "0" + end;
		}
		if(StringUtils.isBlank(UserInfo.start)) {
			//���ô�ʱ��
			UserInfo.start = start;
			System.out.println(nowDate+"��ǩ��ʱ��Ԥ��Ϊ��" + start);
		}
		if(StringUtils.isBlank(UserInfo.end)) {
			UserInfo.end = end;
			System.out.println(nowDate+"��ǩ��ʱ��Ԥ��Ϊ��" + end);
		}
		//��¼
		try {
			String now = Main.getNowMinutes();
			if(UserInfo.start.equals(now) || UserInfo.end.equals(now)) {
				if(!Main.Login(UserInfo.userName,UserInfo.password)) {
					System.out.println("��¼ʧ��");
					return;
				}
				System.out.println("��¼�ɹ�..");
				ClockInfo clock = Main.getUserApplyNo();
				Main.executeClock(clock);
				Main.logout();
				if(UserInfo.start.equals(now)) {
					UserInfo.start = "";
				}
				if(UserInfo.end.equals(now)) {
					UserInfo.end = "";
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * �жϵ�ǰ���������ڼ�
	 * 
	 * @param pTime
	 *            ��Ҫ�жϵ�ʱ��
	 * @return dayForWeek �жϽ��
	 * @Exception �����쳣
	 */
	public static int dayForWeek(String pTime) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(format.parse(pTime));
			int dayForWeek = 0;
			if (c.get(Calendar.DAY_OF_WEEK) == 1) {
				dayForWeek = 7;
			} else {
				dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
			}
			return dayForWeek;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
}
