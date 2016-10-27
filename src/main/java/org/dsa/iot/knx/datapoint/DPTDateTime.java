package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPTDateTime extends DPT {
	private final String format;
	private final String suffix;

	public DPTDateTime(String dptId, String format, String suffix) {
		super(dptId);
		this.format = format;
		this.suffix = suffix;
	}

	@Override
	public int getDataTypeId() {
		return DataTypes.BINARY.ordinal();
	}
}
