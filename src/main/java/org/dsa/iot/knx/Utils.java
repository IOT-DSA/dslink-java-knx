package org.dsa.iot.knx;

import java.util.Arrays;

import tuwien.auto.calimero.GroupAddress;


public class Utils {
	static final String GROUP_ADDRESS_SEPARATOR = "/";
	
	public static <E> String[] enumNames(Class<E> enumData) {
		String valuesStr = Arrays.toString(enumData.getEnumConstants());
		return valuesStr.substring(1, valuesStr.length() - 1).replace(" ", "").split(",");
	}
	
	public static String getGroupName(GroupAddress groupAddress){
		String mainGroup = null;
		String middleGroup = null;
		String group = null;
		if (null != groupAddress) {
			mainGroup = String.valueOf(groupAddress.getMainGroup());
			middleGroup = String.valueOf(groupAddress.getMiddleGroup());
			group = mainGroup + GROUP_ADDRESS_SEPARATOR + middleGroup;
		}
		
		return group;
	}
}
