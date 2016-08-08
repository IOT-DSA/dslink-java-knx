package org.dsa.iot.knx;

import tuwien.auto.calimero.link.KNXNetworkLinkIP;

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
	
	public static int parseServiceMode(TransmissionType type){
		switch (type){
		case Tunneling:
			return KNXNetworkLinkIP.TUNNELING;
		case Routing: 
			return KNXNetworkLinkIP.ROUTING;
		default:
			return KNXNetworkLinkIP.ROUTING;
		}
	}
}
