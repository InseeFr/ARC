package fr.insee.arc.utils.ressourceUtils;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class LogConfigurator {

	private LoggerContext context;

	public LogConfigurator(String logConfiguration) {
		// Using here an XML configuration
		URL log4jprops = this.getClass().getClassLoader().getResource(logConfiguration);
		if (log4jprops != null) {
			context=Configurator.initialize(null, log4jprops.toString());
		} else {
			File file = new File(logConfiguration);
			context=Configurator.initialize(null, file.getAbsolutePath());
		}
	}

	/** Replace ConsoleAppender with FileAppender 
	 * @param logDirectory
	 * @param logFileName 
	 */
	public void configureRollingFileAppender(String logDirectory, String logFileName) {
		// TODO remove xml configuration ??

		Configuration config = context.getConfiguration();
		Appender appender = createRollingFileAppender(config, logDirectory, logFileName);
		appender.start();
		config.addAppender(appender);

		// replace every console appender by the the rollingfileappender
		Map<String, LoggerConfig> loggerConfig = config.getLoggers();
		for (LoggerConfig l:loggerConfig.values())
		{
			for (Appender z:l.getAppenders().values())
			{
				if (z.getClass().isAssignableFrom(ConsoleAppender.class))
				{
					l.removeAppender(z.getName());
					l.addAppender(appender, null, null);
				}
			}
		}

		// apply
		context.updateLoggers();
	}

	/** Create the rolling file appender 
	 * @param logFileName */
	private RollingFileAppender createRollingFileAppender(Configuration config, String logDirectory, String logFileName) {
		return RollingFileAppender.newBuilder()
			.setConfiguration(config)
			.withFileName(logDirectory + File.separator + logFileName + ".log")
			.withFilePattern(logDirectory + File.separator + logFileName + "-%d{MM-dd-yyyy}.log").setFilter((Filter) null)
			.setName("File")
			.withBufferSize(8192)
			.setLayout(
				(Layout<? extends Serializable>) PatternLayout.newBuilder()
				.withPattern("%d %p %c [%t] %m%n").build())
			.withPolicy(
				(TriggeringPolicy) TimeBasedTriggeringPolicy.newBuilder()
				.withInterval(1).withModulate(true).build())
			.build();
		
	}

	public void configureLogLevel(String logLevel) {
		Level level = getLevel(logLevel);
		Map<String, LoggerConfig> loggerConfigs = context.getConfiguration().getLoggers();
		for (LoggerConfig logger :loggerConfigs.values())
		{
			if (logger.getName().equals("fr.insee.arc")) {
				logger.stop();
				logger.setLevel(level);
				logger.start();
			}
		}
	}

	private Level getLevel(String logLevel) {
		return Level.getLevel(logLevel.trim().toUpperCase());
	}

}
