package org.dsa.iot.knx;

public enum GroupAddressType {
	THREE_LEVEL, TWO_LEVEL, FREE;

	public static GroupAddressType parseType(String str) {
		GroupAddressType type;

		switch (str) {
		case "THREE_LEVEL":
			type = GroupAddressType.THREE_LEVEL;
			break;
		case "TWO_LEVEL":
			type = GroupAddressType.TWO_LEVEL;
			break;
		case "FREE":
			type = GroupAddressType.FREE;
			break;
		default:
			type = GroupAddressType.THREE_LEVEL;
		}

		return type;
	}
}
