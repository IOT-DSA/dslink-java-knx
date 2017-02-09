package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPTString extends DPT {
	public DPTString(String dptId) {
		super(dptId);
	}

	@Override
	public int getDataTypeId() {
		return DataTypes.ALPHANUMERIC.ordinal();
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
