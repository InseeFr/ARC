package fr.insee.arc.core.service.p2chargement.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.p2chargement.bo.CSVFormatRules;
import fr.insee.arc.core.service.p2chargement.operation.ParseFormatRulesOperation;
import fr.insee.arc.utils.database.Delimiters;

public class ChargeurCsvRulesStepDao {
	
	private ParseFormatRulesOperation<CSVFormatRules> parser;
	private String sourceTable;
	
	private Map<Integer,List<Integer>> dispatchColumnExpressionByStep;
	private Map<Integer,List<Integer>> dispatchFilterExpressionByStep;
	private Integer numberOfStep;
	
	private List<String> columns;
	private List<String> columnsRenamed;
	
	private int MAX_STEPS = 10;
	
	
	public ChargeurCsvRulesStepDao(ParseFormatRulesOperation<CSVFormatRules> parser, String sourceTable)
	{
		this.parser = parser;
		this.sourceTable = sourceTable;
	}
	

	/**
	 * Suffix of variable that indicates the calculation step
	 * A variable called var$new$ is calculated at step 1
	 * A variable called var$new$2 is calculated at step 2
	 * @param step
	 * @return
	 */
	protected String variableSuffixForStep(Integer step)
	{
		if (step.equals(0))
		{
			return "";
		}
		
		if (step.equals(1))
		{
			return String.format(Delimiters.RENAME_SUFFIX_STEP,"");
		}
		
		return String.format(Delimiters.RENAME_SUFFIX_STEP, step);
	}

	protected String removeStepInformation(String columnName, Integer step)
	{
		return columnName.replace(variableSuffixForStep(step), "");
	}
	
	/**
	 * Dispatch column and filter expressions by step
	 */
	public void prepareCollections()
	{
		/**
		 * Column definition may contain a hint with the step where the variable should be calculated
		 * So we rebuild the raw user expression with the column definition and expression
		 * to detect the right step where the calculus must happen 
		 */
		List<String> columnDefinitionAndExpression = IntStream.range(0, parser.getValues(CSVFormatRules.COLUMN_DEFINITION).size()).boxed()
		.map(i-> CSVFormatRules.columnRawExpression(
					parser.getValues(CSVFormatRules.COLUMN_DEFINITION).get(i)
					, parser.getValues(CSVFormatRules.COLUMN_EXPRESSION).get(i))
				).toList();
		
		
		this.dispatchColumnExpressionByStep = dispatchExpressionByStep(columnDefinitionAndExpression);		
		this.dispatchFilterExpressionByStep = dispatchExpressionByStep(parser.getValues(CSVFormatRules.FILTER_WHERE));
		
		this.numberOfStep = Integer.max(
				dispatchColumnExpressionByStep.isEmpty()?0:Collections.max(this.dispatchColumnExpressionByStep.keySet())
				, dispatchFilterExpressionByStep.isEmpty()?0:Collections.max(this.dispatchFilterExpressionByStep.keySet())
				);
		
		this.columns = new ArrayList<>();
		this.columnsRenamed = new ArrayList<>();

		
		for (Integer step=0; step<=this.numberOfStep; step++)
		{
			if (dispatchColumnExpressionByStep.get(step)!=null)
			{
				for (Integer index : dispatchColumnExpressionByStep.get(step))
				{
					// rework column definition to remove the step hint
					parser.getValues(CSVFormatRules.COLUMN_DEFINITION).set(index, removeStepInformation(parser.getValues(CSVFormatRules.COLUMN_DEFINITION).get(index),step));
					columns.add(parser.getValues(CSVFormatRules.COLUMN_DEFINITION).get(index));
					columnsRenamed.add(parser.getValues(CSVFormatRules.COLUMN_DEFINITION).get(index)+variableSuffixForStep(step+1));
				}
			}
		}
		
	}
	
	
	
	public ArcPreparedStatementBuilder queryColumnExpression(int partitionNumber, String whereExpression)
	{
		if (this.numberOfStep==null)
		{
			prepareCollections();
		}
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		queryColumnExpressionByStep(query, this.numberOfStep, partitionNumber, whereExpression);
		
		return query;
	}
	
	
	/**
	 * recursive method to encapuslate each step
	 * @param query
	 * @param step
	 * @param partitionNumber
	 */
	private void queryColumnExpressionByStep(ArcPreparedStatementBuilder query, Integer step, int partitionNumber, String whereExpression)
	{
		query.build("\n ");
		
		if (step<this.numberOfStep)
		{
			query.build(" ( ");
		}
		
		query.build("SELECT ", stepViewName(step) , ".*");
		
		if (dispatchColumnExpressionByStep.get(step) != null)
		{
			for (Integer index : dispatchColumnExpressionByStep.get(step))
			{
				query.build("\n ,"
						,parser.getValues(CSVFormatRules.COLUMN_EXPRESSION).get(index)
							.replace(Delimiters.PARTITION_NUMBER_PLACEHOLDER, partitionNumber + "000000000000::bigint")
						, " as "
						, parser.getValues(CSVFormatRules.COLUMN_DEFINITION).get(index)
						, variableSuffixForStep(step+1)
						);
				
			}
		}
		query.build("\n FROM ");
		
		if (step==0)
		{
			query.build(sourceTable, " ", stepViewName(step));
		}
			else
		{
			queryColumnExpressionByStep(query, step-1, partitionNumber, whereExpression);
		}
		
		query.build("\n WHERE true ");
		
		if (step==0 && whereExpression!=null)
		{
			query.build("\n ", whereExpression, " ");
		}
		
		
		if (dispatchFilterExpressionByStep.get(step) != null)
		{
			for (Integer index : dispatchFilterExpressionByStep.get(step))
			{
				query.build("\n AND (",parser.getValues(CSVFormatRules.FILTER_WHERE).get(index),")");
			}
		}
		query.build("\n ");
		
		if (step<this.numberOfStep)
		{
			query.build(") ", stepViewName(step+1));
		}
		
	}
	
	String stepViewName(Integer step)
	{
		return "v"+step;
	}
	
	/**
	 * Dispatch column expression by step
	 * @return
	 */
	protected Map<Integer, List<Integer>> dispatchExpressionByStep(List<String> ruleExpression)
	{
		Map<Integer, List<Integer>> dispatchedExpression= new HashMap<>();
			
			for (int i = 0; i < ruleExpression.size(); i++) {
				
				for (int step=MAX_STEPS; step>=0; step--)
				{
					if (step==0)
					{
						if (dispatchedExpression.get(step)==null)
						{
							dispatchedExpression.put(step, new ArrayList<Integer>());
						}
						
						dispatchedExpression.get(step).add(i);
						break;
					}
					
					if (ruleExpression.get(i).contains(variableSuffixForStep(step)))
					{
						if (dispatchedExpression.get(step)==null)
						{
							dispatchedExpression.put(step, new ArrayList<Integer>());
						}
						
						dispatchedExpression.get(step).add(i);
						
						break;					
					}
				}
			
			}
		
		return dispatchedExpression;
		
	}


	public Map<Integer, List<Integer>> getDispatchColumnExpressionByStep() {
		return dispatchColumnExpressionByStep;
	}


	public Map<Integer, List<Integer>> getDispatchFilterExpressionByStep() {
		return dispatchFilterExpressionByStep;
	}


	public Integer getNumberOfStep() {
		return numberOfStep;
	}


	public List<String> getColumns() {
		return columns;
	}


	public List<String> getColumnsRenamed() {
		return columnsRenamed;
	}


	
	
}
