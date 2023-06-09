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
import fr.insee.arc.utils.structure.GenericBean;

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
	
	private static final class ArrayOfArrayProvider extends EntityProvider<ArrayList<ArrayList<String>>> {
		@Override
		public ArrayList<ArrayList<String>> apply(ResultSet res) {
			try {
				return fromResultSetToArray(res);
			} catch (ArcException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	public static final EntityProvider<ArrayList<ArrayList<String>>> getArrayOfArrayProvider() {
		return new ArrayOfArrayProvider();
	}

	private static final class GenericBeanProvider extends EntityProvider<GenericBean> {
		@Override
		public GenericBean apply(ResultSet res) {
			return new GenericBean(getArrayOfArrayProvider().apply(res));
		}
	}

	public static final EntityProvider<GenericBean> getGenericBeanProvider() {
		return new GenericBeanProvider();
	}

	private static final class TypedListProvider<T> extends EntityProvider<List<T>> {
		private Function<ResultSet, T> orm;

		/**
		 * @param orm
		 */
		TypedListProvider(Function<ResultSet, T> orm) {
			this.orm = orm;
		}

		@Override
		public List<T> apply(ResultSet res) {
			try {
				return fromResultSetToListOfT(() -> new ArrayList<>(), this.orm, res);
			} catch (ArcException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	public static final <T> EntityProvider<List<T>> getTypedListProvider(Function<ResultSet, T> orm) {
		return new TypedListProvider<>(orm);
	}

	private static final class DefaultEntityProvider<T> extends EntityProvider<T> {
		private Function<ResultSet, T> orm;

		/**
		 * @param orm
		 */
		DefaultEntityProvider(Function<ResultSet, T> orm) {
			this.orm = orm;
		}

		@Override
		public T apply(ResultSet res) {
			return this.orm.apply(res);
		}
	}

	public static final <T> EntityProvider<T> getDefaultEntityProvider(Function<ResultSet, T> orm) {
		return new DefaultEntityProvider<>(orm);
	}

	public static final GenericBean fromResultSetToGenericBean(ResultSet res) {
		try {
			return new GenericBean(fromResultSetToArray(res));
		} catch (ArcException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public static ArrayList<ArrayList<String>> fromResultSetToArray(ResultSet res) throws ArcException {
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

	public static <T, U extends List<T>> U fromResultSetToListOfT(Supplier<U> newList, Function<ResultSet, T> orm,
			ResultSet res) throws ArcException {
		try {
			U result = newList.get();
			while (res.next()) {
				result.add(orm.apply(res));
			}
			return result;
		} catch (SQLException sqlException) {
			throw new ArcException(sqlException, ArcExceptionMessage.SQL_EXECUTE_FAILED).logFullException();
		}
	}
}