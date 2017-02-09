package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPT1BitControlled extends DPT {
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
	public String getLabel(boolean isZero) {
		return isZero ? this.zeroLabel : this.oneLabel;
	}

	@Override
	public String getUnit() {
		// TODO Auto-generated method stub
		return null;
	}
}