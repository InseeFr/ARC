package fr.insee.arc.web.gui.query.dao;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;

public class QueryDao extends VObjectHelperDao {
	
	private VObjectService vObjectService;
	private DataObjectService dataObjectService;

	public QueryDao(VObjectService vObjectService, DataObjectService dataObjectService) {
		super();
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}
	
	/**
	 * dao call to build query vobject
	 * 
	 * @param viewWsContext
	 */
	public void initializeQuery(VObject viewQuery, String myQuery) {
		HashMap<String, String> defaultInputFields = new HashMap<>();

		if (myQuery!=null){
			String m=myQuery.trim();
			if (m.endsWith(";"))
			{
				m=m.substring(0, m.length()-1);
			}

			ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder(m);
			
			if (Boolean.TRUE.equals(UtilitaireDao.get(0).testResultRequest(null, query)))
			{
				this.vObjectService.initialize(viewQuery, query, "arc.ihm_query", defaultInputFields);
			}
			else
			{
				try {
					UtilitaireDao.get(0).executeImmediate(null, myQuery);
					this.vObjectService.destroy(viewQuery);
					viewQuery.setMessage("query.complete");
				} catch (Exception e) {
					this.vObjectService.destroy(viewQuery);
					viewQuery.setMessage(e.getMessage());
				}

			}
		}
	}
	
	/**
	 * dao call to build tables vobject
	 * 
	 * @param viewWsContext
	 */
	public void initializeTable(VObject viewTable, String mySchema) {
        ViewEnum dataModelTable = ViewEnum.PG_TABLES;
		String nameOfViewTable = dataObjectService.getView(dataModelTable);
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.TABLENAME, SQL.FROM, nameOfViewTable);
		query.build(SQL.WHERE, ColumnEnum.SCHEMANAME, "='" + mySchema + "'");
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewTable, query, "arc.ihm_table", defaultInputFields);
	}
	
	public static String queryTableSelected(String mySchema, String tableName) {
		return "select * from " + mySchema + "." + tableName + " limit 10 ";
	}

}
