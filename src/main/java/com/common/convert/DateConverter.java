package com.common.convert;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import ognl.DefaultTypeConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
/***
 * 日期转换器.<br>用于struts2
 * 
 * @author huangwei
 *
 */
public class DateConverter extends DefaultTypeConverter {
	private static final Logger logger = Logger.getLogger(DateConverter.class);

	private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final String DATETIME_PATTERN_NO_SECOND = "yyyy-MM-dd HH:mm";
	private static final String DATETIME_PATTERN_ZH ="yyyy年MM月dd日 HH点mm分ss秒";
	private static final String DATETIME_PATTERN_ZH2 ="yyyy年MM月dd日 HH时mm分ss秒";
	private static final String DATE_PATTERN = "yyyy-MM-dd";

	private static final String MONTH_PATTERN = "yyyy-MM";

	/**
	 * Convert value between types
	 */
	@SuppressWarnings("unchecked")
	public Object convertValue(Map ognlContext, Object value, Class toType) {
		Object result = null;
		// java.sql.Date 是java.util.Date 的子类
		if (toType == java.util.Date.class) {
			try {
				result = doConvertToDate(value,  toType);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (toType == java.sql.Timestamp.class) {
			try {
				java.util.Date date=doConvertToDate(value, toType);
				result = new Timestamp(date.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (toType == String.class) {
			result = doConvertToString(value);
		}
		return result;
	}

	/**
	 * Convert String to Date
	 * 
	 * @param value
	 * @return
	 * @throws ParseException
	 */
	private java.util.Date doConvertToDate(Object value,Class toType) throws ParseException {
		java.util.Date result = null;

		if (value instanceof String) {
			result = DateUtils.parseDate((String) value, new String[] {DATETIME_PATTERN,
					DATE_PATTERN,  MONTH_PATTERN 
					,DATETIME_PATTERN_NO_SECOND,DATETIME_PATTERN_ZH,DATETIME_PATTERN_ZH2});
//			if(toType==java.sql.Timestamp.class){
//				result=new java.sql.Timestamp(result.getTime());
//			}
			// all patterns failed, try a milliseconds constructor
			if (result == null && StringUtils.isNotEmpty((String) value)) {

				try {
					result = new Date(new Long((String) value).longValue());
				} catch (Exception e) {
					logger.error("Converting from milliseconds to Date fails!");
					e.printStackTrace();
				}

			}

		} else if (value instanceof Object[]) {
			// let's try to convert the first element only
			Object[] array = (Object[]) value;

			if ((array != null) && (array.length >= 1)) {
				value = array[0];
				result = doConvertToDate(value,toType);
			}

		} else if (Date.class.isAssignableFrom(value.getClass())) {
			result = (Date) value;
		}
		return (java.util.Date)result;
	}

	/**
	 * Convert Date to String
	 * 
	 * @param value
	 * @return
	 */
	private String doConvertToString(Object value) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				DATETIME_PATTERN);
		String result = (String)value;
		if (value instanceof Date) {
			result = simpleDateFormat.format(value);
		}
		return result;
	}
}
