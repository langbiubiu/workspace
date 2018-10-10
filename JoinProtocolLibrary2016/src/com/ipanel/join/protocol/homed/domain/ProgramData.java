package com.ipanel.join.protocol.homed.domain;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class ProgramData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1021878939634747880L;
	@Expose
	private String program_id;
	@Expose
	private String program_name;
	@Expose
	private String program_type;
	@Expose
	private String start_time;
	@Expose
	private String end_time;

	public String getProgram_id() {
		return program_id;
	}

	public void setProgram_id(String program_id) {
		this.program_id = program_id;
	}

	public String getProgram_name() {
		return program_name;
	}

	public void setProgram_name(String program_name) {
		this.program_name = program_name;
	}

	public String getProgram_type() {
		return program_type;
	}

	public void setProgram_type(String program_type) {
		this.program_type = program_type;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public String getEnd_time() {
		return end_time;
	}

	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}

}
