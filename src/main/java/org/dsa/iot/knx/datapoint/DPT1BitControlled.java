package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPT1BitControlled extends DPT implements DatapointStatus {
	private final String zeroLabel;
	private final String oneLabel;

	public DPT1BitControlled(String dptId, String zeroLabel, String oneLabel) {
		super(dptId);
		this.zeroLabel = zeroLabel;
		this.oneLabel = oneLabel;
	}

	@Override
	public int getDataTypeId() {
		return DataTypes.NUMERIC.ordinal();
	}

	@Override
	public String getStatusLabel(boolean isOff) {
		return isOff ? this.zeroLabel : this.oneLabel;
	}
}