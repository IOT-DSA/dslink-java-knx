package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPTDate extends DPT {
	private final String format;
	private final String suffix;

	public DPTDate(String dptId, String format, String suffix) {
		super(dptId);
		this.format = format;
		this.suffix = suffix;
	}

	@Override
	public int getDataTypeId() {
		return DataTypes.BINARY.ordinal();
	}

	@Override
	public String getLabel(boolean isZero) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUnit() {
		// TODO Auto-generated method stub
		return null;
	}
}