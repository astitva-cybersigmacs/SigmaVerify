package com.cybersigma.sigmaverify.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {

	public static Date convertToDate(String strDate){
		DateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        try {
            return (Date)formatter.parse(strDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

	}
public static Date convertToDate(String strDate, String pattern){
		DateFormat formatter = new SimpleDateFormat(pattern);
        try {
            return (Date)formatter.parse(strDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

	}

	public static String currentDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime now = LocalDateTime.now();
		// System.out.println(dtf.format(now));
		return dtf.format(now);
	}

	/**
	 * Get the current Date
	 * 
	 * @author mayankjyotiverma
	 * @return current date
	 */
	public static Date today() {
		// TODO: add the months in the current date and return the Date
		Date date = new Date();
		try {
			LocalDate now = LocalDate.now();
			// System.out.println(now);

			Instant instant = Instant.from(now.atStartOfDay(ZoneId.of("GMT")));
			date = Date.from(instant);
			//System.out.println(date);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return date;
	}

	/**
	 * Add the month in the given date
	 * @author mayankjyotiverma
	 * @param date
	 * @param addMonth
	 * @return Date date
	 */
	public static Date addMonthsInDate(Date date, int addMonth) {
		// TODO Auto-generated method stub
		LocalDate givenDate = LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault()).plusMonths(addMonth);
		Instant instant = Instant.from(givenDate.atStartOfDay(ZoneId.of("GMT")));
        return Date.from(instant);
	}
	public Date addDaysInDate(Date date, int days) {
		// TODO Auto-generated method stub
		LocalDate givenDate = LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault()).plusDays(days);
		Instant instant = Instant.from(givenDate.atStartOfDay(ZoneId.of("GMT")));
        return Date.from(instant);
	}

	public String addYearsInDate(String date, int addYear) {
		// TODO addYearsInDate required two parameter Date and years to add
		LocalDate localDate = LocalDate.parse(date).plusYears(addYear).minusDays(1);
		// System.out.println("Expiry date " + localDate);
		return localDate.toString();
	}

	public String addMonthsInDate(String date, int addMonth) {
		// TODO addMonthsInDate required two parameter Date and month to add
		LocalDate localDate = LocalDate.parse(date).plusMonths(addMonth);
		// System.out.println("Expiry date " + localDate);
		return localDate.toString();
	}

	public String addDaysInDate(String date, int addDays) {
		// TODO addMonthsInDate required two parameter Date and days to add
		LocalDate localDate = LocalDate.parse(date).plusDays(addDays);
		// System.out.println("Expiry date " + localDate);
		return localDate.toString();
	}

	public static Date addMonthsInDate(int months) {
		// TODO: add the months in the current date and return the Date
		Date date = new Date();
		try {
			LocalDate now = LocalDate.now().plusMonths(months);
		//	System.out.println(now);

			Instant instant = Instant.from(now.atStartOfDay(ZoneId.of("GMT")));
			date = Date.from(instant);
		//	System.out.println(date);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return date;
	}
	public static Date subtractMinutesInDate(int minutes) {
			// TODO: add the months in the current date and return the Date
			Date date = new Date();
			try {
				LocalDateTime now = LocalDateTime.now().minusMinutes(minutes);

				Instant instant = Instant.from(now.atZone(ZoneId.of("GMT+0530")));
				date = Date.from(instant);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return date;
		}

	public static boolean asvScanDateComp(Date quarterDate) {
		boolean compDate = false;

		LocalDate now = LocalDate.now().plusDays(7);
		// System.out.println("today +7 "+now);

		Date date = new Date();
		Instant instant = Instant.from(now.atStartOfDay(ZoneId.of("GMT")));
		date = Date.from(instant);

		// System.out.println("date "+date);
		// System.out.println("quarterDate "+quarterDate);

//		if(quarterDate.after(date)) {
//			compDate= true;
//			System.out.println("insiide  "+quarterDate);
//		}

		String current = now.toString();
		String qurDate = quarterDate.toString();
		if (current.trim().equalsIgnoreCase(qurDate.trim())) {
			// System.out.println("str comp current "+current +"qurDate "+qurDate);
			compDate = true;
		}

		return compDate;
	}

	public static String convertToIndianTime(String giventimeZoneOffset, String givendate, String giventime)
			throws ParseException {
//		
//		giventimeZoneOffset = "(UTC+14:00) Hawaii)";
//		givendate = "2023-10-06";
//		giventime = "12:48 am";
		int timeZoneDiff = 0;

		giventime = convertToAmPm(giventime);
	//	System.out.println(giventime);

		if (giventimeZoneOffset.charAt(4) == '-') {
			timeZoneDiff = (Integer.parseInt(giventimeZoneOffset.substring(5, 7)) * 60
					+ Integer.parseInt(giventimeZoneOffset.substring(8, 10))) + 330;
		} else {
			timeZoneDiff = 330 - (Integer.parseInt(giventimeZoneOffset.substring(5, 7)) * 60
					+ Integer.parseInt(giventimeZoneOffset.substring(8, 10)));
		}

		// System.out.println("Time Diff " + timeZoneDiff);

		String str = givendate + " " + giventime;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime dateTime = LocalDateTime.parse(str, formatter).plusMinutes(timeZoneDiff);

		DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a EEEE");

		//System.out.println(dateTime.format(formatterDateTime));

		return dateTime.format(formatterDateTime).toString().replace('T', ' ');

	}

	public static String convertToAmPm(String time) throws ParseException {
		SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
		SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
		Date date = parseFormat.parse(time);
		// giventime = displayFormat.format(date);

		return "" + displayFormat.format(date);
	}

	public static int checkShift() {

		LocalTime currentTime = LocalTime.now();

		if (currentTime.isAfter(LocalTime.parse("10:00")) && currentTime.isBefore(LocalTime.parse("13:00"))) {
			return 1; // Vikalp
		}

		if (currentTime.isAfter(LocalTime.parse("13:00")) && currentTime.isBefore(LocalTime.parse("16:00"))) {
			return 2; // Sakshi
		}

		if (currentTime.isAfter(LocalTime.parse("16:00")) && currentTime.isBefore(LocalTime.parse("19:00"))) {
			return 3; // Manshi
		} else {
			int max = 3, min = 1; // return shift random
			//System.out.println("Generated numbers are within " + min + " to " + max);
		//	System.out.println(min + (int) (Math.random() * ((max - min) + 1)));

			return (min + (int) (Math.random() * ((max - min) + 1)));
		}

	}
	public static String dateToken() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now).replaceAll("-","").replaceAll(":","");
	}
	public static Date last24HoursDateTime(){
		return new Date(new Date().getTime()-24*60*60*1000);
	}
	public static Date addOrSubHours(char action, long hours){
		if (action=='A')
			return new Date(new Date().getTime()+hours*60*60*1000);
		return new Date(new Date().getTime()-hours*60*60*1000);
	}

}
