package fr.insee.arc_composite.web.action;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class MyInterceptor implements Interceptor {

	    private static final long serialVersionUID = 1L;
	 
	    public String intercept(ActionInvocation invocation) throws Exception {
	 
	        String className = invocation.getAction().getClass().getName();
	        
	        long startTime = System.currentTimeMillis();
	        System.out.println("Before calling action: " + className);
	 
	        String result = invocation.invoke();
	 
	        long endTime = System.currentTimeMillis();
	        System.out.println("After calling action: " + className
	                + " Time taken: " + (endTime - startTime) + " ms");
//	 
//	        
//			Method[] m=invocation.getAction().getClass().getDeclaredMethods();
//			
//			for (int i=0;i<m.length;i++)
//			{
//				if (m[i].getName().equals("doIntercept"))
//				{
//					System.out.println("goo");
//					m[i].invoke(invocation.getAction().getClass(), null);
//				}
//			}
//			
//			Class noparams[] = {};
//			//call the printIt method
//			Method method = cls.getDeclaredMethod("doIntercept", noparams);
//			if (method!=null){
//		//		method.invoke(obj, null);
//			}
//	        
	        return result;
	    }
	 
	    public void destroy() {
//	        System.out.println("Destroying MyLoggingInterceptor...");
	    }
	    public void init() {
//	        System.out.println("Initializing MyLoggingInterceptor...");
	    }
	}
	
	
