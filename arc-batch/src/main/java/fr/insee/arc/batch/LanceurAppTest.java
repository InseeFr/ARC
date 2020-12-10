package fr.insee.arc.batch;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class LanceurAppTest {

	public static void main(String[] args) throws Exception {
    	
		
		 ApplicationContext context = new AnnotationConfigApplicationContext(BatchConfig.class);
		 context.getBean(AppTest.class).execute(args);

    	
	}

}
