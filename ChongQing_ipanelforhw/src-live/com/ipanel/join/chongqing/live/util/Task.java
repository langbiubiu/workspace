package com.ipanel.join.chongqing.live.util;

public class Task {
	public int id;
	public Object obj1;
//	public Object obj2;
//	public Object obj3;
	public int type;

	public boolean valid(int start,int end,int size){
		if(start>end){
			return id<=end||id>=start-size+end;
		}else{
			return id>=start&&id<=start+size-1;
		}
	}

//	public Task(int id, Object obj1, Object obj2, Object obj3, int type) {
//		super();
//		this.id = id;
//		this.obj1 = obj1;
//		this.obj2 = obj2;
//		this.obj3 = obj3;
//		this.type = type;
//	}
	
	public Task(int id, Object obj1, int type) {
		super();
		this.id = id;
		this.obj1 = obj1;
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + type;
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
		Task other = (Task) obj;
		if (id != other.id)
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
}
