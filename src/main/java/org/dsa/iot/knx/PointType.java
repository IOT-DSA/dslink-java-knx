package org.dsa.iot.knx;

public enum PointType {
	BOOL, CONTROL, FLOAT2, FLOAT4, STRING, UNSIGNED;

	public static PointType parseType(String str) {
		PointType type;

		switch (str) {
		case "BOOL":
			type = PointType.BOOL;
			break;
		case "CONTROL":
			type = PointType.CONTROL;
			break;
		case "FLOAT2":
			type = PointType.FLOAT2;
			break;
		case "FLOAT4":
			type = PointType.FLOAT4;
			break;
		case "UNSIGNED":
			type = PointType.UNSIGNED;
			break;
		default:
			type = PointType.STRING;
		}

		return type;
	}

	public static PointType getDataTypeByDataSize(String measurement) {
		PointType type;

		switch (measurement) {
		case "1 Bit":
			type = PointType.BOOL;
			break;
		case "4 Bit":
			type = PointType.CONTROL;
			break;
		case "1 Byte":
			type = PointType.UNSIGNED;
			break;
		case "2 Byte":
			type = PointType.UNSIGNED;
			break;
		case "4 Byte":
			type = PointType.UNSIGNED;
			break;
		default:
			type = PointType.STRING;
			break;
		}

		return type;
	}
}
