package fr.insee.arc.utils.webutils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import fr.insee.arc.utils.dao.UtilitaireDao;

public class WebUtils {
    
    private WebUtils() {
    	throw new IllegalStateException("Utility class");
    }

    /** Returns a healthcheck description.*/
    public static Map<String,Object> getHealthCheckStatus(){
    	Map<String,Object> map = new HashMap<>();
    	String status;
    	if (UtilitaireDao.isConnectionOk("arc")) {
    		status = "up";
    	} else {
    		status = "down";
    	}
    	map.put("status", status);

    	Map<String, Object> details = new HashMap<>();
    	map.put("details", details);
    	HashMap<String, String> dbHealthCheck = new HashMap<>();
		details.put("dataBaseHealthCheck", dbHealthCheck);
		dbHealthCheck.put("status", status);
		return map;
    }
    
    public static String getCookie(HttpServletRequest request, String key) {
        String value = "";

        Cookie[] c = request.getCookies();

        if (c == null) {
            return value;
        }

        for (int i = 0; i < c.length; i++) {
            if (c[i].getName().equals(key)) {
                return c[i].getValue();
            }
        }
        return value;
    }

    /**
     * Create or replace a cookie
     *
     * @param key
     * @param value
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String key, String value) {
        Cookie[] cookies = request.getCookies();

        boolean foundCookie = false;

        // update the cookie
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(key)) {
                    foundCookie = true;
                    if (value == null) {
                        cookies[i].setMaxAge(0);
                    } else {
                        cookies[i].setMaxAge(1 * 24 * 60 * 60 * 1000);
                    }

                    cookies[i].setValue(value);
                    response.addCookie(cookies[i]);
                    break;
                }
            }
        }

        // create it 
        if (!foundCookie && StringUtils.isNotEmpty(value)) {

                Cookie cookie1 = new Cookie(key, value);
                cookie1.setHttpOnly(true);
                cookie1.setMaxAge(1 * 24 * 60 * 60 * 1000);
                response.addCookie(cookie1);
        }

    }

    /**
     * ajoute une valeur à la chaine du cookie existant avec un séparateur
     *
     * @param key
     * @param value
     */
    public static void setCookieAdditive(HttpServletRequest request, HttpServletResponse response, String key, String value) {
        String s = getCookie(request, key);

        String separator = ":";

        if (s.equals("")) {
            setCookie(request, response, key, value);
        } else {
            int i = 0;
            String[] t = s.split(separator);
            StringBuilder target= new StringBuilder();
            boolean found = false;

            for (i = 0; i < t.length; i++) {
                if (t[i].equals(value)) {
                    found = true;
                    break;
                }
                target.append(separator);
            }

            if (!found) {
        	target.append(value);
            }

            setCookie(request, response, key, target.toString());
        }
    }

}
