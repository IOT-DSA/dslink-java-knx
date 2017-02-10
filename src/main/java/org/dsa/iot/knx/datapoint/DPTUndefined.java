package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPTUndefined extends DPT {
	public DPTUndefined() {
		super("0.000");
	}

	@Override
	public int getDataTypeId() {
		return DataTypes.BINARY.ordinal();
	}

}
