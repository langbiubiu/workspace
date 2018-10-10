package com.ipanel.join.chongqing.live.data;

public class ColumeData {
	private int id;
	private String name;
	private Object tag;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public ColumeData(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public ColumeData() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumeData other = (ColumeData) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
