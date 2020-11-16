package fr.insee.arc.batch;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:applicationContext.xml")
public class BatchConfig {
	// empty for now, everything is in applicationContext.xml
}
