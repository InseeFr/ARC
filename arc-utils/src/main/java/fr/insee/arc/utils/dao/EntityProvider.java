package fr.insee.arc.utils.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

/**
 * 
 * Classe bridge qui permet d'utiliser l'interface de {@link UtilitaireDao} dans
 * d'autres classes du projet.<br/>
 * 
 * @param <T>
 */
public abstract class EntityProvider<T> implements Function<ResultSet, T> {

	private EntityProvider()
	{
		super();
	}
	
	private static final class ArrayOfArrayProvider extends EntityProvider<List<List<String>>> {
		@Override
		public List<List<String>> apply(ResultSet res) {
			try {
				return fromResultSetToArray(res);
			} catch (ArcException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	public static final EntityProvider<List<List<String>>> getArrayOfArrayProvider() {
		return new ArrayOfArrayProvider();
	}

	public static List<List<String>> fromResultSetToArray(ResultSet res) throws ArcException {
		return fromResultSetToList(() -> new ArrayList<>(), new ArrayList<>(), res);
	}

	public static <T extends List<String>, U extends List<T>> U fromResultSetToList(Supplier<T> newList, U result,
			ResultSet res) throws ArcException {
		try {
			ResultSetMetaData rsmd = res.getMetaData();
			T record = newList.get();
			// Noms des colonnes
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				record.add(rsmd.getColumnLabel(i));
			}
			result.add(record);
			// Types des colonnes
			record = newList.get();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				/*
				 * le ResultSetMetaData fait un peu n'importe quoi avec les types. Si on a un
				 * int/bigint + sequence, il renvoit une serial/bigserial. sauf que l'on n'en
				 * veut pas, alors on doit corriger ça à la main
				 */
				HashMap<String, String> correctionType = new HashMap<>();
				correctionType.put("serial", "int4");
				correctionType.put("bigserial", "int8");
				if (correctionType.containsKey(rsmd.getColumnTypeName(i))) {
					record.add(correctionType.get(rsmd.getColumnTypeName(i)));
				} else {
					record.add(rsmd.getColumnTypeName(i));
				}
			}
			result.add(record);
			while (res.next()) {
				record = newList.get();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					record.add(res.getString(i));
				}
				result.add(record);
			}
			return result;
		} catch (SQLException sqlException) {
			throw new ArcException(sqlException, ArcExceptionMessage.SQL_EXECUTE_FAILED).logFullException();
		}
	}

}