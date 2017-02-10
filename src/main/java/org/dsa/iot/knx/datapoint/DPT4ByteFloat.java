package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPT4ByteFloat extends DPT implements DatapointUnit {
	private final String format;
	private final String suffix;

	public DPT4ByteFloat(String dptId, String format, String suffix) {
		super(dptId);
		this.format = format;
		this.suffix = suffix;
	}

	@Override
	public int getDataTypeId() {
		return DataTypes.NUMERIC.ordinal();
	}

	@Override
	public String getUnit() {
		return this.suffix;
	}
}
