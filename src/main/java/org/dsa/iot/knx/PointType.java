package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.value.ValueType;

public enum PointType {
	BOOL, CONTROL, UNSIGNED, FLOAT2, FLOAT4, TIME, DATE, STRING;

	public static PointType parseType(String str) {
		PointType type = null;

		switch (str) {
		case "BOOL":
			type = PointType.BOOL;
			break;
		case "CONTROL":
			type = PointType.CONTROL;
			break;
		case "UNSIGNED":
			type = PointType.UNSIGNED;
			break;
		case "FLOAT2":
			type = PointType.FLOAT2;
			break;
		case "FLOAT4":
			type = PointType.FLOAT4;
			break;
		case "TIME":
			type = PointType.TIME;
			break;
		case "DATE":
			type = PointType.DATE;
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
			type = PointType.FLOAT2;
			break;
		case "4 Byte":
			type = PointType.FLOAT4;
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
			type = PointType.FLOAT2;
			break;
		case "2bytef":
			type = PointType.FLOAT2;
			break;
		case "4byte":
			type = PointType.FLOAT4;
			break;
		case "4byteu":
			type = PointType.FLOAT4;
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

	public static ValueType getValueType(PointType type) {
		ValueType vt = ValueType.STRING;

		if (type == PointType.BOOL) {
			vt = ValueType.BOOL;
		} else if (type == PointType.CONTROL) {
			vt = ValueType.NUMBER;
		} else if (type == PointType.FLOAT2 || type == PointType.FLOAT4) {
			vt = ValueType.NUMBER;
		} else if (type == PointType.UNSIGNED) {
			vt = ValueType.NUMBER;
		} else if (type == PointType.TIME) {
			vt = ValueType.TIME;
		} else if (type == PointType.DATE) {
			vt = ValueType.TIME;
		} else if (type == PointType.STRING) {
			vt = ValueType.STRING;
		}

		return vt;
	}
}
