package fr.insee.arc.ws.services.restServices.setRules.pojo;

import java.util.List;
import java.util.Map;

import fr.insee.arc.utils.structure.Record;

/**
 * Parameters for ARC steps execution
 * @author MS
 *
 */
public class SetRulesPojo {

	
	// environnement d'execution
	public String sandbox;
	
	// 
	public String targetRule;
	
	// action à réaliser
	public SetRulesActionEnum actionType;
	
	// data content
	public Map<String,Record> content;

	public String getSandbox() {
		return sandbox;
	}

	public void setSandbox(String sandbox) {
		this.sandbox = sandbox;
	}

	public String getTargetRule() {
		return targetRule;
	}

	public void setTargetRule(String targetRule) {
		this.targetRule = targetRule;
	}

	public SetRulesActionEnum getActionType() {
		return actionType;
	}

	public void setActionType(SetRulesActionEnum actionType) {
		this.actionType = actionType;
	}

	public Map<String, Record> getContent() {
		return content;
	}

	public void setContent(Map<String, Record> content) {
		this.content = content;
	}

	
	
}
