package fr.insee.arc.web.gui.query.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;

@Component
public class QueryDao extends VObjectHelperDao {

	/**
	 * dao call to build query vobject
	 * 
	 * @param viewWsContext
	 */
	public void initializeQuery(VObject viewQuery, Integer myDbConnection, String myQuery) {
		Map<String, String> defaultInputFields = new HashMap<>();

		if (myQuery == null || myQuery.isEmpty()) {
			this.vObjectService.destroy(viewQuery);
			return;
		}

		String m = myQuery.trim();
		if (m.endsWith(";")) {
			m = m.substring(0, m.length() - 1);
		}

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder(m);

		if (Boolean.TRUE.equals(UtilitaireDao.get(myDbConnection).testResultRequest(null, query))) {
			this.vObjectService.setConnectionIndex(myDbConnection);
			this.vObjectService.initialize(viewQuery, query, "arc.ihm_query", defaultInputFields);
			this.vObjectService.resetConnectionIndex();

		} else {

			query = new ArcPreparedStatementBuilder();

			try {
				UtilitaireDao.get(myDbConnection).executeRequest(null, myQuery);
				query.build(SQL.SELECT, query.quoteText("query succeed"), SQL.AS, "query_result");
			} catch (Exception e) {
				query.build(SQL.SELECT, query.quoteText(e.getMessage()), SQL.AS, "query_result");
			}

			this.vObjectService.initialize(viewQuery, query, "arc.ihm_query", defaultInputFields);

		}

	}

	/**
	 * dao call to build tables vobject
	 * 
	 * @param viewWsContext
	 */
	public void initializeTable(VObject viewTable, Integer myDbConnection, String mySchema) {
		ViewEnum dataModelTable = ViewEnum.PG_TABLES;
		String nameOfViewTable = dataObjectService.getView(dataModelTable);
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.TABLENAME, SQL.FROM, nameOfViewTable);
		query.build(SQL.WHERE, ColumnEnum.SCHEMANAME, "='" + mySchema + "'");
		// default value
		Map<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.setConnectionIndex(myDbConnection);
		vObjectService.initialize(viewTable, query, "arc.ihm_table", defaultInputFields);
		this.vObjectService.resetConnectionIndex();
	}

	public static String queryTableSelected(String mySchema, String tableName) {
		return "select * from " + mySchema + "." + tableName + " limit 10 ";
	}

}
