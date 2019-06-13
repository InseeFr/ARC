package fr.insee.arc_essnet.core;

public class Main {

    public static void main(String[] args) {
	System.out.println(new Exception("test exception").toString().replace("'", "''").replaceAll("\r", ""));
    }

}
