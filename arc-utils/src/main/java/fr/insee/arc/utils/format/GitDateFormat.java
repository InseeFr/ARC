package fr.insee.arc.utils.format;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class GitDateFormat {

	
	public static ZonedDateTime parse(String gitVersionDate)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
		ZonedDateTime zonedDateTime;
		try {
		zonedDateTime = ZonedDateTime.parse(gitVersionDate, formatter);
		}
		catch (Exception e)
		{
			zonedDateTime = ZonedDateTime.parse("1970-01-01T00:00:01+0200", formatter);
		}
		
		return zonedDateTime;
	}

	
	
}
