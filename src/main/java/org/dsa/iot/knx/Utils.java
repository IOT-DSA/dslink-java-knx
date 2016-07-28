package org.dsa.iot.knx;

import java.util.Arrays;

public class Utils {
	public static <E> String[] enumNames(Class<E> enumData) {
		String valuesStr = Arrays.toString(enumData.getEnumConstants());
		return valuesStr.substring(1, valuesStr.length() - 1).replace(" ", "").split(",");
	}
}
