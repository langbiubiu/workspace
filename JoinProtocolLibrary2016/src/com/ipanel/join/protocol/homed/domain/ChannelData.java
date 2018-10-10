package com.ipanel.join.protocol.homed.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;

public class ChannelData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4591828174489086564L;
	@Expose
	private String chlId;
	@Expose
	private String chlName;
	@Expose
	private String chlNumber;
	@Expose
	private String isTimeShifting;
	@Expose
	private String ownStatus;
	@Expose
	private String demand_url_part;
	@Expose
	private ChlCfg chlCfg;
	@Expose
	private String subTypeID;
	@Expose
	private String current_program;
	@Expose
	private List<ProgramData> program_list = new ArrayList<ProgramData>();

	public String getChlId() {
		return chlId;
	}

	public void setChlId(String chlId) {
		this.chlId = chlId;
	}

	public String getChlName() {
		return chlName;
	}

	public void setChlName(String chlName) {
		this.chlName = chlName;
	}

	public String getChlNumber() {
		return chlNumber;
	}

	public void setChlNumber(String chlNumber) {
		this.chlNumber = chlNumber;
	}

	public String getIsTimeShifting() {
		return isTimeShifting;
	}

	public void setIsTimeShifting(String isTimeShifting) {
		this.isTimeShifting = isTimeShifting;
	}

	public String getOwnStatus() {
		return ownStatus;
	}

	public void setOwnStatus(String ownStatus) {
		this.ownStatus = ownStatus;
	}

	public String getDemand_url_part() {
		return demand_url_part;
	}

	public void setDemand_url_part(String demand_url_part) {
		this.demand_url_part = demand_url_part;
	}

	public ChlCfg getChlCfg() {
		return chlCfg;
	}

	public void setChlCfg(ChlCfg chlCfg) {
		this.chlCfg = chlCfg;
	}

	public String getSubTypeID() {
		return subTypeID;
	}

	public void setSubTypeID(String subTypeID) {
		this.subTypeID = subTypeID;
	}

	public String getCurrent_program() {
		return current_program;
	}

	public void setCurrent_program(String current_program) {
		this.current_program = current_program;
	}

	public List<ProgramData> getProgram_list() {
		return program_list;
	}

	public void setProgram_list(List<ProgramData> program_list) {
		this.program_list = program_list;
	}

}
