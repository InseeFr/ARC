package fr.insee.arc.batch;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class LanceurARC {

	public static void main(String[] args) {
		
		 ApplicationContext context = new AnnotationConfigApplicationContext(BatchConfig.class);
		 context.getBean(BatchARC.class).execute(args);
		 

	}

}
