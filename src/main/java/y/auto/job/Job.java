package y.auto.job;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import y.auto.entity.ClockInfo;
import y.auto.entity.UserInfo;
import y.auto.main.Main;
import y.auto.util.Config;
import y.auto.util.HolidayUtil;

public class Job implements org.quartz.Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("�����˿�ʼִ��...��" + Main.getNow() + "��ǩ��ʱ�䣺" + UserInfo.start + ",ǩ��ʱ�䣺" + UserInfo.end);
        String nowDate = Main.getNowDate("YYYYMMdd");
        int week = HolidayUtil.isWorkDay(nowDate);
        if (week != 0) {
            System.out.println("���ǹ����գ����򿨡�" + nowDate + "��");
            return;
        }
        String start = getStart();
        String end = getEnd();
        if (StringUtils.isBlank(UserInfo.start)) {
            //���ô�ʱ��
            UserInfo.start = start;
            System.out.println(nowDate + "��ǩ��ʱ��Ԥ��Ϊ��" + start);
        }
        if (StringUtils.isBlank(UserInfo.end)) {
            UserInfo.end = end;
            System.out.println(nowDate + "��ǩ��ʱ��Ԥ��Ϊ��" + end);
        }
        //��¼
        try {
            String now = Main.getNowMinutes();
            if (UserInfo.start.equals(now) || UserInfo.end.equals(now)) {
                if (!Main.Login(UserInfo.userName, UserInfo.password)) {
                    System.out.println("��¼ʧ��");
                    return;
                }
                System.out.println("��¼�ɹ�..");
                ClockInfo clock = Main.getUserApplyNo();
                Main.executeClock(clock);
                Main.logout();
                if (UserInfo.start.equals(now)) {
                    UserInfo.start = "";
                }
                if (UserInfo.end.equals(now)) {
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
     * @param pTime ��Ҫ�жϵ�ʱ��
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * �ж��Ǻ����绹�����磨���Ϊ��0�������� ���Ϊ��1�������磩
     *
     * @return
     */
    public static int amOrpm() {
        GregorianCalendar ca = new GregorianCalendar();
        return ca.get(GregorianCalendar.AM_PM);
    }


    /**
     * ��Χȡ�����
     *
     * @return
     */
    public static int getRandom(int min, int max) {
        int ran2 = (int) (Math.random() * (max - min) + min);
        return ran2;
    }

    public static String[] getRandomConfig(int index) {
        String random = Config.getInstance().getConfig("random") + "";
        if (StringUtils.isNotBlank(random)) {
            return random.split(",")[index].split("-");
        }
        return null;
    }

    public static String[] getStartConfig() {
        return getRandomConfig(0);
    }

    public static String[] getEndConfig() {
        return getRandomConfig(1);
    }

    public static String getEnd() {
        String[] endConfig = getEndConfig();
        int r = amOrpm();
        String end = "30";
        if (r == 0) {//����
        } else {//����
            end = getRandom(Integer.parseInt(endConfig[0]), Integer.parseInt(endConfig[1])) + "";
        }
        if (end.length() < 2) {
            end = "0" + end;
        }
        return end;
    }

    public static String getStart() {
        String[] startConfig = getStartConfig();
        int r = amOrpm();
        String start = "01";
        if (r == 0) {//����
            start = getRandom(Integer.parseInt(startConfig[0]), Integer.parseInt(startConfig[1])) + "";
        }
        if (start.length() < 2) {
            start = "0" + start;
        }
        return start;
    }
}
