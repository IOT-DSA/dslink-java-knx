package org.dsa.iot.knx.project;

import java.util.List;
import java.util.ArrayList;
import org.dsa.iot.knx.EditableFolder;
import org.dsa.iot.knx.datapoint.DatapointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class OpcFileParser extends KnxProjectParser {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(OpcFileParser.class);
	}

	static final String DATA_POINT_SIZE_BOOLING = "1 Bit";
	static final String DATA_POINT_SIZE_PRIORITY_CONTROL = "2 Bit";
	static final String DATA_POINT_SIZE_STEP_CONTROL = "4 Bit";
	static final String DATA_POINT_SIZE_EIGHT_BIT_UNSIGNED = "1 Byte";
	static final String DATA_POINT_SIZE_TWO_BYTE_FLOAT = "2 Byte";
	static final String DATA_POINT_SIZE_THREE_BYTE_UNSIGNED = "3 Byte";
	static final String DATA_POINT_SIZE_FOUR_BYTE_FLOAT = "4 Byte";
	static final String DATA_POINT_SIZE_FOURTEEN_BYTE_UNSIGNED = "14 Byte";

	public OpcFileParser(EditableFolder folder) {
		super(folder);
	}

	public void parse(String content) {
		LOGGER.info("start parsing opc file...");
		String[] lines = content.split(System.getProperty("line.separator"));

		// skip the title and build the hash map: path => nodes
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];
			String[] records = line.split("\\.");
			String dataSize = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
			mainGroupName = records[0];
			middleGroupName = records[1];
			String[] addressAndNames = records[2].split("\t");
			addressStr = addressAndNames[0];

			GroupAddress groupAddress = null;
			try {
				groupAddress = new GroupAddress(addressStr);
			} catch (KNXFormatException e) {
				e.printStackTrace();
			} finally {
				if (null == groupAddress) {
					return;
				}
			}

			String subGroupName = addressAndNames[1];
			String[] dataPointAndPath = parseGroupAddress(subGroupName);
			String dataPointName = dataPointAndPath[0];
			String path = dataPointAndPath[1];
			String dataPointType = getDataPointTypeByDataSize(dataSize);
			buildAddressToBean(mainGroupName, middleGroupName, groupAddress, dataPointType, dataPointName);

			if (!pathToNodes.containsKey(path)) {
				List<GroupAddress> nodes = new ArrayList<>();
				nodes.add(groupAddress);
				pathToNodes.put(path, nodes);
			} else {
				List<GroupAddress> nodes = pathToNodes.get(path);
				nodes.add(groupAddress);
				pathToNodes.put(path, nodes);
			}
		}

		buildGroupTree();
		LOGGER.info("parsing is done!");
	}

	private String getDataPointTypeByDataSize(String sizeInBit) {
		DatapointType type = null;
		switch (sizeInBit) {
		case DATA_POINT_SIZE_BOOLING:
			type = DatapointType.BOOLEAN_BOOLEAN;
			break;
		case DATA_POINT_SIZE_PRIORITY_CONTROL:
			type = DatapointType.TWO_BIT_BOOLEAN_CONTROL;
			break;
		case DATA_POINT_SIZE_STEP_CONTROL:
			type = DatapointType.FOUR_BIT_CONTROL_BLINDS;
			break;
		case DATA_POINT_SIZE_EIGHT_BIT_UNSIGNED:
			type = DatapointType.EIGHT_BIT_UNSIGNED_PERCENT;
			break;
		case DATA_POINT_SIZE_TWO_BYTE_FLOAT:
			type = DatapointType.TWO_BYTE_FLOAT_TEMPDIFF;
			break;
		case DATA_POINT_SIZE_THREE_BYTE_UNSIGNED:
			type = DatapointType.RGB;
			break;
		case DATA_POINT_SIZE_FOUR_BYTE_FLOAT: {
			type = DatapointType.FOUR_BYTE_FLOAT_ABSOLUTE_TEMPERATURE;
			break;
		}
		case DATA_POINT_SIZE_FOURTEEN_BYTE_UNSIGNED:
			type = DatapointType.STRING_8859_1;
			break;
		default:
			type = DatapointType.UNDEFINED;
			break;
		}
		String dataPointTypeNme = type.toString();
		return dataPointTypeNme;
	}
}
