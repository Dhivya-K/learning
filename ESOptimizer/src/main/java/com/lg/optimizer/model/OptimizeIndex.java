package com.lg.optimizer.model;

public class OptimizeIndex {

	public String granularity;
	public String shards_available;
	public boolean changegranularity;
	public boolean increase_shards;
	
	public boolean isChangegranularity() {
		return changegranularity;
	}

	

	public void setChangegranularity(boolean changegranularity) {
		this.changegranularity = changegranularity;
	}

	public boolean isChange_shards() {
		return change_shards;
	}

	public void setChange_shards(boolean change_shards) {
		this.change_shards = change_shards;
	}

	public boolean change_shards;
	public String name;
	public String mem_Size;
	public int size;

	public String getGranularity() {
		return granularity;
	}

	public void setGranularity(String granularity) {
		this.granularity = granularity;
	}

	public String getShards_available() {
		return shards_available;
	}

	public void setShards_available(String shards_available) {
		this.shards_available = shards_available;
	}

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMem_Size() {
		return mem_Size;
	}

	public void setMem_Size(String mem_Size) {
		this.mem_Size = mem_Size;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}



	@Override
	public String toString() {
		return "OptimizeIndex [" + (granularity != null ? "granularity=" + granularity + ", " : "")
				+ (shards_available != null ? "shards_available=" + shards_available + ", " : "") + "changegranularity="
				+ changegranularity + ", increase_shards=" + increase_shards + ", change_shards=" + change_shards + ", "
				+ (name != null ? "name=" + name + ", " : "") + (mem_Size != null ? "mem_Size=" + mem_Size + ", " : "")
				+ "size=" + size + "]";
	}

}
