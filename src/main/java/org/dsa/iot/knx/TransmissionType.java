package org.dsa.iot.knx;

public enum TransmissionType {
	Tunneling, Routing;

	public static TransmissionType parseType(String str) {
		TransmissionType type;

		switch (str) {
		case "Tunneling":
			type = TransmissionType.Tunneling;
			break;
		case "Routing":
			type = TransmissionType.Routing;
			break;
		default:
			type = TransmissionType.Routing;
		}

		return type;
	}
}
