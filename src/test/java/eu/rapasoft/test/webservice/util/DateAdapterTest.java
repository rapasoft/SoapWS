package eu.rapasoft.test.webservice.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author rap
 */
public class DateAdapterTest {

	@Test
	public void testParseDate() throws Exception {
		Date expectedDate = Date.from(LocalDate.of(2001, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Date parsedDate = DateAdapter.parseDate("2001-01-01");

		assertEquals(expectedDate, parsedDate);
	}

	@Test
	public void testPrintDate() throws Exception {
		Date inputDate = Date.from(LocalDate.of(2001, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

		String expectedString = "2001-01-01";
		String printedString = DateAdapter.printDate(inputDate);

		assertEquals(expectedString, printedString);
	}
}