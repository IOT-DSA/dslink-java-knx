package org.dsa.iot.knx;

public enum PointType {
	BOOL, CONTROL, FLOAT2, FLOAT4, STRING, UNSIGNED, SIGNED;

	public static PointType parseType(String str) {
		PointType type = null;

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
		case "SIGNED":
			type = PointType.SIGNED;
			break;
		case "STRING":
			type = PointType.STRING;
			break;
		default:
			break;
		}

		return type;
	}

	public static PointType getDataTypeByDataPointType(String measurement) {
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
		case "boolean":
			type = PointType.BOOL;
			break;
		case "control":
			type = PointType.CONTROL;
			break;
		case "8bitu":
			type = PointType.UNSIGNED;
			break;
		case "2byteu":
			type = PointType.UNSIGNED;
			break;
		case "2bytef":
			type = PointType.FLOAT2;
			break;
		case "4byte":
			type = PointType.SIGNED;
			break;
		case "4byteu":
			type = PointType.UNSIGNED;
			break;
		case "4bytef":
			type = PointType.FLOAT4;
			break;
		default:
			type = PointType.STRING;
			break;
		}

		return type;
	}
}
