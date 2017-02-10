package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPT2ByteFloat extends DPT implements DatapointUnit {
	private final String format;
	private final String suffix;

	public DPT2ByteFloat(String dptId, String format, String suffix) {
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
