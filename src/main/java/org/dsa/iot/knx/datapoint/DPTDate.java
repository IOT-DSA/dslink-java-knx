package org.dsa.iot.knx.datapoint;

import org.dsa.iot.knx.DataTypes;

public class DPTDate extends DPT {
    private final String zeroLabel;
    private final String oneLabel;

    public DPTDate(String dptId, String zeroLabel, String oneLabel) {
        super(dptId);
        this.zeroLabel = zeroLabel;
        this.oneLabel = oneLabel;
    }

    @Override
    public int getDataTypeId() {
        return DataTypes.BINARY.ordinal();
    }
}