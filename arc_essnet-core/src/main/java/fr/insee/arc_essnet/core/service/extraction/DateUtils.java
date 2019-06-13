package fr.insee.arc_essnet.core.service.extraction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;

/**
 * Utilty class to handler date
 * @author S4lwo8
 *
 */
public class DateUtils {

	private static final String DD_M_YYYY_HH_MM_SS = "dd-M-yyyy hh:mm:ss";

	private static final Logger LOGGER = Logger.getLogger(DateUtils.class);
	
	private static final String YYYYMMDD = "yyyyMMdd";
	
	private static final String YYYY_MM_DD ="yyyy-MM-dd";
	
	private static final List<String> Mois=Arrays.asList("JANVIER","FEVRIER","MARS","AVRIL","MAI","JUIN","JUILLET","AOUT","SEPTEMBRE","OCTOBRE","NOVEMBRE","DECEMBRE");
	
	
	  private DateUtils() {
	      throw new IllegalStateException("Utility class");
	    }
	  
	
	public static Date newDateAAAA_MM_JJ(String date) {
  		try {
			return  new SimpleDateFormat(YYYY_MM_DD).parse(date);
		} catch (ParseException e) {

			LOGGER.warn("Méthode newDateAAAAMMJJ - Echec lors de la transforamtion en date de cette valeur: "+date);
			return null;
		}		
		
	}
	
	
	
	public static Date newDateAAAAMMJJ(String date) {
  		try {
  		  return  new SimpleDateFormat(YYYYMMDD).parse(date);
		} catch (ParseException e) {

			LOGGER.warn("Méthode newDateAAAAMMJJ - Echec lors de la transforamtion en date de cette valeur: "+date);
			return null;
		}		
		
	}
	
	/**
	 * 
	 * @return  numero et mois de validité 
	 */
	public static Map<String, String> getAvailableValidities(){
		
		Map<String,String> validitesDisponibles = new TreeMap<>();
		LocalDateTime aujourdhui = new LocalDateTime();
		
		/*
		 * Last validity process 
		 * -> current month-2 if before the 18th                     
		 * -> current month-1 otherwise
		 */

		 LocalDateTime lastValidite = null;
		 if (aujourdhui.getDayOfMonth()<18){
			 lastValidite = aujourdhui.minusMonths(2);
		 }else{
			 lastValidite = aujourdhui.minusMonths(1);
		 }
		 
		 //Get 4 month back
		 int key = 1;
		 for(int i = 3; i >= 0; i--){
			 
			 
			 String mois = lastValidite.minusMonths(i).monthOfYear().getAsText(Locale.FRENCH);
			 mois = mois.substring(0, 1).toUpperCase() + mois.substring(1);
			 String annee = lastValidite.minusMonths(i).year().getAsText(Locale.FRENCH);
			 String validite = mois+" "+annee;
			 validitesDisponibles.put(String.valueOf(key),validite);
			 key++;
			 
		 }
		 
		 
		
		return validitesDisponibles;
		
		
		
	}
	
	/**
	 * Convert a date in monther/year format to year/month/day with day = 01
	 * @param dateIHM
	 * @return the date in desired format
	 */
	private static String transformValidityDateToFormatFile(String dateIHM){
		
		String dateNouveauFormat;
		
		String moisIHM =  dateIHM.substring(0, dateIHM.length()-4)
								 .trim()
								 .toUpperCase()
								 .replace("É", "E")
								 .replace("Û", "U");
		String mois;
		if(Mois.indexOf(moisIHM)+1<10){
			mois = "0"+(Mois.indexOf(moisIHM)+1);
		}else{
			mois = String.valueOf((Mois.indexOf(moisIHM)+1));
		}
		String annee = dateIHM.substring(dateIHM.length() - 4, dateIHM.length());
		String jours = "01";

		dateNouveauFormat=annee+"-"+mois+"-"+jours;
		return dateNouveauFormat;
	}
	
	/**
	 * Get all month in validity between month start end month end
	 * For instance, monthStart = january 2016, monthEnd = april 2016, the method will return
	 * january, february, march and april 2016 if this month are in the available validities
	 * @param monthStart
	 * @param monthEnd
	 * @return all month in validity between month start end month end
	 * 
	 */
	public static List<String> obtenirPeriode(String monthStart, String monthEnd){
		
		List<String> periode = new ArrayList<>();
		Map<String,String> availablevalitities = getAvailableValidities();
		int nombreValiditeDisponible = availablevalitities.size();
		
		
		int startNumberInMap=0;
		int endNumberInMap=0;
		
		for (Entry<String, String> entry : availablevalitities.entrySet()) {
		        if (monthStart.equals(entry.getValue())) {
		        	startNumberInMap = Integer.parseInt(entry.getKey());
		        }else if (monthEnd!=null && monthEnd.equals(entry.getValue())){
		        	endNumberInMap= Integer.parseInt(entry.getKey());
		        }
		}
		
		if (endNumberInMap==0){
			for (int i = startNumberInMap ; i <= nombreValiditeDisponible; i++){
				
				addDateToPeriode(periode, availablevalitities, i);
			}			
		}else{
			//on itere depuis numeroDebutDansMap jusqu'au numeroFinDansMap
			for (int i = startNumberInMap ; i <= endNumberInMap; i++){
				addDateToPeriode(periode, availablevalitities, i);
			}			
		}
		 
		 
		return periode;
		
	}



	private static void addDateToPeriode(List<String> periode, Map<String, String> validitesDisponibles, int i) {
		
		String validityDate = validitesDisponibles.get(String.valueOf(i));
		
		//Transform in file format
		String fileFormatValidityDate = transformValidityDateToFormatFile(validityDate);
		periode.add(fileFormatValidityDate);
	}



	public static Date getOlderValiditeDate(List<String> validitesSelectionnees) {
		
		Date olderValidity =  null;
		for (String testValidity : validitesSelectionnees) {
				
			//initialize older validity
			if (olderValidity == null){
				olderValidity = newDateAAAA_MM_JJ(testValidity);
			}else{
				//compate current validity with current older validity
				Date validity = newDateAAAA_MM_JJ(testValidity);
				if(validity!=null && validity.before(olderValidity)){
					olderValidity = validity;
				}
			}
		}
		
		return olderValidity;
		
	}
	
	

	
}
