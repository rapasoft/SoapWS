package eu.rapasoft.test.webservice.util;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author rap
 *
 * This class is used for conversion between {@link javax.xml.datatype.XMLGregorianCalendar} and {@link Date}
 * when jaxb2 processes the XSD.
 *
 */
public class DateAdapter {

	public static Date parseDate(String s) {
		return DatatypeConverter.parseDate(s).getTime();
	}

	public static String printDate(Date dt) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);
		return DatatypeConverter.printDate(cal).split("\\+")[0];
	}

}