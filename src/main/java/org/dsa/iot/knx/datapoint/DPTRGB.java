package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPTRGB extends DPT {
	private final String lower;
	private final String upper;

	public DPTRGB(String dptId, String lower, String upper) {
		super(dptId);
		this.lower = lower;
		this.upper = upper;
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