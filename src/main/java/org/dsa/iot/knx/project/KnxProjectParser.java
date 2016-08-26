package org.dsa.iot.knx.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.knx.EditableFolder;
import org.dsa.iot.knx.GroupAddressBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;

public abstract class KnxProjectParser {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(KnxProjectParser.class);
	}

	static final String BUILDING = "Building";

	EditableFolder folder;

	Map<String, List<GroupAddress>> pathToNodes = new HashMap<>();
	Map<GroupAddress, GroupAddressBean> addressToBean = new HashMap<>();

	String mainGroupName = null;
	String middleGroupName = null;
	String addressStr = null;

	public KnxProjectParser(EditableFolder folder) {
		this.folder = folder;
	}

	public void buildAddressToBean(String mainGroupName, String middleGroupName, GroupAddress groupAddress,
			String dataPointType, String dataPointName) {
		GroupAddressBean bean = new GroupAddressBean();

		bean.setMainGroup(mainGroupName);
		bean.setMiddleGroup(middleGroupName);
		bean.setGroupAddress(groupAddress.toString());
		bean.setDataPointType(dataPointType);
		bean.setDataPointName(dataPointName);
		addressToBean.put(groupAddress, bean);
	}

	public void buildGroupTree() {
		Iterator it = pathToNodes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String path = (String) pair.getKey();
			String[] subDirectories = path.split("_");
			Queue<String> queue = new LinkedList<>(Arrays.asList(subDirectories));
			Node lastNode = folder.buildFolderTree(folder.getNode(), queue);

			List<GroupAddress> nodes = (ArrayList<GroupAddress>) pair.getValue();
			for (GroupAddress groupAddress : nodes) {
				GroupAddressBean bean = addressToBean.get(groupAddress);
				folder.buildDataPoint(lastNode, bean);
			}
		}
	}

	public String[] parseGroupAddress(String groupAddress) {
		String[] nameArray = groupAddress.split("_");
		String dataPointName = nameArray[0] + "_" + nameArray[1] + "_" + nameArray[2];
		String path = groupAddress.substring(dataPointName.length() + 1);

		return new String[] { dataPointName, path };
	}

	public abstract void parseItems(String content);

}
