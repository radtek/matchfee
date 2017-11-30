package org.wxjs.matchfee.modules.charge.entity;

import org.wxjs.matchfee.common.persistence.DataEntity;

public class Project extends DataEntity<Project> {
	
	private static final long serialVersionUID = 1L;
	
	private String prjNum;

	private String prjName;

	private String buildCorpName;

	private String buildCorpCode;

	private String prjAddress;
	
	public Project(){
		
	}

	public String getPrjNum() {
		return prjNum;
	}



	public void setPrjNum(String prjNum) {
		this.prjNum = prjNum;
	}

	public String getPrjName() {
		return prjName;
	}

	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}

	public String getBuildCorpName() {
		return buildCorpName;
	}

	public void setBuildCorpName(String buildCorpName) {
		this.buildCorpName = buildCorpName;
	}

	public String getBuildCorpCode() {
		return buildCorpCode;
	}

	public void setBuildCorpCode(String buildCorpCode) {
		this.buildCorpCode = buildCorpCode;
	}

	public String getPrjAddress() {
		return prjAddress;
	}

	public void setPrjAddress(String prjAddress) {
		this.prjAddress = prjAddress;
	}

}
