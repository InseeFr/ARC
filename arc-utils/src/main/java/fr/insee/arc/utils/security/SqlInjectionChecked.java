package fr.insee.arc.utils.security;

public @interface SqlInjectionChecked {
	
	 public String[] requiredAsSafe() default "";
	
}
