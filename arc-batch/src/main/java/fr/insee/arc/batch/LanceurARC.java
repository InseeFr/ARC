package fr.insee.arc.batch;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LanceurARC {

	public static void main(String[] args) {
		
		 ApplicationContext context = 
		            new ClassPathXmlApplicationContext("applicationContext.xml");

		 context.getBean(BatchARC.class).execute(args);

	}

}
