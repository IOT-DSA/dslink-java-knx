package org.dsa.iot.knx;

public class GroupAddressBean {
	private String mainGroup;
	private String middleGroup;
	private String groupAddress;
	private String dataPointName;
	private boolean settable;
	private String dataType;
	private String dataPointType;

	public String getMainGroup() {
		return mainGroup;
	}

	public void setMainGroup(String mainGroup) {
		this.mainGroup = mainGroup;
	}

	public String getMiddleGroup() {
		return middleGroup;
	}

	public void setMiddleGroup(String middleGroup) {
		this.middleGroup = middleGroup;
	}

	public String getGroupAddress() {
		return groupAddress;
	}

	public void setGroupAddress(String groupAddress) {
		this.groupAddress = groupAddress;
	}

	public String getDataPointName() {
		return dataPointName;
	}

	public void setDataPointName(String name) {
		this.dataPointName = name;
	}

	public boolean isSettable() {
		return settable;
	}

	public void setSettable(boolean settable) {
		this.settable = settable;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataPointType() {
		return dataPointType;
	}

	public void setDataPointType(String dataPointType) {
		this.dataPointType = dataPointType;
	}

}
