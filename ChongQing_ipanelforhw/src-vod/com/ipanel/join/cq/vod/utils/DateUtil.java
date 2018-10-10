package com.ipanel.join.cq.vod.utils;

import java.util.Calendar;

public class DateUtil {
	
	// �ж��Ƿ�Ϊ����  
    public static boolean isLeapYear(int year) {  
        if (year % 100 == 0 && year % 400 == 0) {  
            return true;  
        } else if (year % 100 != 0 && year % 4 == 0) {  
            return true;  
        }  
        return false;  
    }  
	
  //ָ��ĳ���е�ĳ�µĵ�һ�������ڼ�  
    public static int getWeekdayOfMonth(int year, int month){  
        Calendar cal = Calendar.getInstance();  
        cal.set(year, month-1, 1);  
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)-1;  
        return dayOfWeek;  
    } 
    
	//�õ�ĳ���ж������� 
    public static int getDaysOfMonth(boolean isLeapyear, int month) { 
    	int daysOfMonth = 0;
        switch (month) { 
        case 1: 
        case 3: 
        case 5: 
        case 7: 
        case 8: 
        case 10: 
        case 12: 
            daysOfMonth = 31; 
            break; 
        case 4: 
        case 6: 
        case 9: 
        case 11: 
            daysOfMonth = 30; 
            break; 
        case 2: 
            if (isLeapyear) { 
                daysOfMonth = 29; 
            } else { 
                daysOfMonth = 28; 
            } 
 
        } 
        return daysOfMonth; 
    } 
}
