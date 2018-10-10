package com.ipanel.join.cq.vod.jsondata;

import java.util.ArrayList;

import com.ipanel.join.protocol.sihua.cqvod.VodFilm;

public class VodSearchFilm {	

	String countTotal;
	ArrayList<VodFilm> array;
	
	public String getCountTotal() {
		return countTotal;
	}
	public void setCountTotal(String countTotal) {
		this.countTotal = countTotal;
	}
	public ArrayList<VodFilm> getArray() {
		return array;
	}
	public void setArray(ArrayList<VodFilm> array) {
		this.array = array;
	}
	public VodSearchFilm(String countTotal, ArrayList<VodFilm> array) {
		super();
		this.countTotal = countTotal;
		this.array = array;
	}
	public VodSearchFilm(){}
	
	
}
