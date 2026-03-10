package fr.insee.arc.batch;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class LanceurAppTestTemp {

	public static void main(String[] args) throws Exception {
    	
		
		try(AbstractApplicationContext  context = new AnnotationConfigApplicationContext(BatchConfig.class);)
		{
		 context.getBean(AppTestTemp.class).execute(args);
		}
		 
	}

}
