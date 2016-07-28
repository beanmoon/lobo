package com.jd.lobo.bean.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by lutong on 7/28/16.
 */
public class Test {
	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date();
		cal.setTime(curDate);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Date date1 = cal.getTime();
		System.out.println(sdf.format(curDate));
		System.out.println(sdf.format(date1));

		List<Integer> list = new ArrayList<>();
		for(int i = 0; i < 10; i++)
			list.add(i);
		System.out.println(list.subList(0, 10));
	}
}
