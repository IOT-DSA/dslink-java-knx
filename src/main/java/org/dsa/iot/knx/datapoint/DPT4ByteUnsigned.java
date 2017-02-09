package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPT4ByteUnsigned extends DPT {
	private final String format;
	private final String suffix;

	public DPT4ByteUnsigned(String dptId, String format, String suffix) {
		super(dptId);
		this.format = format;
		this.suffix = suffix;
	}

	@Override
	public int getDataTypeId() {
		return DataTypes.NUMERIC.ordinal();
	}

	@Override
	public String getLabel(boolean isZero) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUnit() {
		return this.suffix;
	}
}
