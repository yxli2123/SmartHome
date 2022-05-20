package com.vanch.vhxdemo;

public class Epc {
	private String id;
	private int count;
	private int ixuhao;
	private String WATag;
	private String WATID;
	private String WACiShu;//次数
	
	public Epc(String id, int count, int ixuhao, String WATag, String WATID, String WACiShu) {
		this.id = id;
		this.count = count;
		this.ixuhao = ixuhao;
		this.WATag = WATag;
		this.WATID = WATID;
		this.WACiShu = WACiShu;//次数
	}

	public Epc() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getixuhao() {
		return ixuhao;
	}

	public void setixuhao(int ixuhao) {
		this.ixuhao = ixuhao;
	}

	public String getWATag() {
		return WATag;
	}

	public void setWATag(String WATag) {
		this.WATag = WATag;
	}

	public String getWATID() {
		return WATID;
	}

	public void setWATID(String WATID) {
		this.WATID = WATID;
	}

	public String getWACiShu() {
		return WACiShu;
	}

	public void setWACiShu(String WACiShu) {
		this.WACiShu = WACiShu;
	}


	@Override
	public String toString() {
		return "Epc [id=" + id + ", count=" + count + "]";
	}
}
