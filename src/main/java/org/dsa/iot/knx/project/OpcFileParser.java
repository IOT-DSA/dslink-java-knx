package org.dsa.iot.knx.project;

import java.util.List;
import java.util.ArrayList;
import org.dsa.iot.knx.EditableFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class OpcFileParser extends KnxProjectParser {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(OpcFileParser.class);
	}

	public OpcFileParser(EditableFolder folder){
		super(folder);
	}
	
	public void parseItems(String content) {

		String[] lines = content.split(System.getProperty("line.separator"));

		// skip the title and build the hash map: path => nodes
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];
			String[] records = line.split("\\.");
			String dataPointType = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
			mainGroupName = records[0];
			middleGroupName = records[1];
			String[] addressAndMore = records[2].split("\t");
			addressStr = addressAndMore[0];
			
			GroupAddress groupAddress = null;
			try {
				groupAddress = new GroupAddress(addressStr);
			} catch (KNXFormatException e) {
				e.printStackTrace();
			} finally {
				if (null == groupAddress){
					return;
				}
			}

			String subGroupName = addressAndMore[1];
			int buidlingIndex = subGroupName.indexOf(BUILDING);
			String path = subGroupName.substring(buidlingIndex);
			String dataPointName = subGroupName.substring(0, buidlingIndex - 1);
			
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
	}
}
