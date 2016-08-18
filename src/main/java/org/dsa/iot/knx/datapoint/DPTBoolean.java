package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPTBoolean extends DPT {
    private final String zeroLabel;
    private final String oneLabel;

    public DPTBoolean(String dptId, String zeroLabel, String oneLabel) {
        super(dptId);
        this.zeroLabel = zeroLabel;
        this.oneLabel = oneLabel;
    }

    @Override
    public int getDataTypeId() {
        return DataTypes.BINARY.ordinal();
    }
}
