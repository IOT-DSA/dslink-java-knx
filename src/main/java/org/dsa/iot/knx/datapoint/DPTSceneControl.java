package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPTSceneControl extends DPT implements DatapointRange {
	private final String lower;
	private final String upper;

	public DPTSceneControl(String dptId, String lower, String upper) {
		super(dptId);
		this.lower = lower;
		this.upper = upper;
	}

	@Override
	public int getDataTypeId() {
		return DataTypes.BINARY.ordinal();
	}

	@Override
	public String getLower() {
		return this.lower;
	}

	@Override
	public String getUpper() {
		return this.upper;
	}
}
