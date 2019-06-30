package fr.insee.arc.utils.webutils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

public class WebUtils {
    
    private WebUtils() {
	throw new IllegalStateException("Utility class");
    }

    public static String getCookie(String key) {
        String value = "";

        Cookie[] c = ServletActionContext.getRequest().getCookies();

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
    public static void setCookie(HttpServletResponse response, String key, String value) {
        Cookie[] cookies = ServletActionContext.getRequest().getCookies();

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
    public static void setCookieAdditive(HttpServletResponse response, String key, String value) {
        String s = getCookie(key);

        String separator = ":";

        if (s.equals("")) {
            setCookie(response, key, value);
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

            setCookie(response, key, target.toString());
        }
    }

}
