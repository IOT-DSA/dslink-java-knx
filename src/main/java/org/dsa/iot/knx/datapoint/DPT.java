package org.dsa.iot.knx.datapoint;

import tuwien.auto.calimero.dptxlator.DPTXlator;

abstract public class DPT {
    private final String dptId;

    DPT(String dptId) {
        this.dptId = dptId;
    }

    public String getDtpId() {
        return dptId;
    }

    abstract public int getDataTypeId();

    protected byte[] getKnxBytes(DPTXlator xl8or) {
        byte[] b = new byte[2 + xl8or.getTypeSize()];
        if (xl8or.getTypeSize() == 0)
            xl8or.getData(b, 1);
        else
            xl8or.getData(b, 2);
        return b;
    }
}
