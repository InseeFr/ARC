package fr.insee.arc.batch;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class LanceurARC {

	public static void main(String[] args) {
		
		try(AbstractApplicationContext  context = new AnnotationConfigApplicationContext(BatchConfig.class);)
		{
			context.getBean(BatchARC.class).execute();	
		}

	}

}
