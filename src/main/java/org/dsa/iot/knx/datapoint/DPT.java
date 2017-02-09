package org.dsa.iot.knx.datapoint;

import tuwien.auto.calimero.dptxlator.DPTXlator;

abstract public class DPT {
	private final String dptId;

	static final int FULL_OFFSET = 2;
	static final int LOWER_OFFSET = 1;

	DPT(String dptId) {
		this.dptId = dptId;
	}

	public String getDtpId() {
		return dptId;
	}

	abstract public int getDataTypeId();

	abstract public String getLabel(boolean isZero);

	abstract public String getUnit();

	protected byte[] getKnxBytes(DPTXlator xl8or) {
		byte[] b = new byte[FULL_OFFSET + xl8or.getTypeSize()];
		if (xl8or.getTypeSize() == 0)
			xl8or.getData(b, LOWER_OFFSET);
		else
			xl8or.getData(b, FULL_OFFSET);
		return b;
	}
}
