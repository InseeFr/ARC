package fr.insee.arc.utils.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class GitDateFormatTest {

	@Test
	public void testParse() {
		
		assertEquals("2024-04-11T13:25:42+02:00", GitDateFormat.parse("2024-04-11T13:25:42+0200").toString());
		assertEquals("1970-01-01T00:00:01+02:00", GitDateFormat.parse("not a date").toString());

		// if date is provided in a good format, it must be superior than date not in good format
		assertEquals(1, GitDateFormat.parse("2024-04-11T13:25:42+0200").compareTo(GitDateFormat.parse("not a date")));
		
	}

}
