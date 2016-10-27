package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPT64BitSigned extends DPT{
	private final String format;
	private final String suffix;

    public DPT64BitSigned(String dptId, String format, String suffix) {
        super(dptId);
        this.format = format;
        this.suffix = suffix;
    }

    @Override
    public int getDataTypeId() {
        return DataTypes.BINARY.ordinal();
    }
}
