package com.sms800.quickwin.domain;

import java.io.Serializable;

public class CustomerTemplateDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private String stateCd;
	private String areaCd;
	private String date;
	private char actvInd;
	
	public CustomerTemplateDTO() {
		// TODO Auto-generated constructor stub
	}
	
	public CustomerTemplateDTO(String stateCd, String areaCd, String date, char actvInd) {
		this.stateCd=stateCd;
		this.areaCd=areaCd;
		this.date=date;
		this.actvInd=actvInd;
	}
	
	public String getStateCd() {
		return stateCd;
	}
	public void setStateCd(String stateCd) {
		this.stateCd = stateCd;
	}
	public String getAreaCd() {
		return areaCd;
	}
	public void setAreaCd(String areaCd) {
		this.areaCd = areaCd;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public char getActvInd() {
		return actvInd;
	}
	public void setActvInd(char actvInd) {
		this.actvInd = actvInd;
	}
	
	
}
