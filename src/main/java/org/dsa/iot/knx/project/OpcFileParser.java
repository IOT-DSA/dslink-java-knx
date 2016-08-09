package org.dsa.iot.knx.project;

import java.io.IOException;
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
import tuwien.auto.calimero.exception.KNXFormatException;

public class OpcFileParser {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(OpcFileParser.class);
	}

	private String content;
	private EditableFolder folder;

	public OpcFileParser(EditableFolder folder, String content) {
		this.folder = folder;
		this.content = content;
	}

	public void parseItems() {
		Map<String, ArrayList<GroupAddress>> pathToNodes = new HashMap<String, ArrayList<GroupAddress>>();
		Map<GroupAddress, GroupAddressBean> addressToBean = new HashMap<GroupAddress, GroupAddressBean>();

		String[] lines = content.split(System.getProperty("line.separator"));
		String mainGroup = null;
		String middleGroup = null;
		String address = null;

		// skip the title and build the hash map: path => nodes
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];
			String[] records = line.split("\\.");
			String dataSize = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
			mainGroup = records[0];
			middleGroup = records[1];
			String[] addressAndMore = records[2].split("\t");
			address = addressAndMore[0];
			GroupAddress groupAddress = null;
			try {
				groupAddress = new GroupAddress(address);
			} catch (KNXFormatException e) {
				e.printStackTrace();
			}

			String nodeAndPath = addressAndMore[1];
			String[] subDirectories = nodeAndPath.split("_");
			String nodeName = subDirectories[0] + ("_") + subDirectories[1] + ("_") + subDirectories[2];

			GroupAddressBean bean = new GroupAddressBean();
			bean.setMainGroup(mainGroup);
			bean.setMiddleGroup(middleGroup);
			bean.setGroupAddress(groupAddress.toString());
			bean.setDataSize(dataSize);
			bean.setName(nodeName);
			addressToBean.put(groupAddress, bean);

			String path = nodeAndPath.substring(
					subDirectories[0].length() + 1 + subDirectories[1].length() + 1 + subDirectories[2].length() + 1);

			if (!pathToNodes.containsKey(path)) {
				ArrayList<GroupAddress> nodes = new ArrayList<>();
				nodes.add(groupAddress);
				pathToNodes.put(path, nodes);
			} else {
				ArrayList<GroupAddress> nodes = pathToNodes.get(path);
				nodes.add(groupAddress);
				pathToNodes.put(path, nodes);
			}
		}

		// build the folder tree from the hashMap
		Iterator it = pathToNodes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String path = (String) pair.getKey();
			String[] subDirectories = path.split("_");
			Queue<String> queue = new LinkedList<>(Arrays.asList(subDirectories));
			Node lastNode = folder.buildFolderTree(folder.getNode(), queue);

			ArrayList<GroupAddress> nodes = (ArrayList<GroupAddress>) pair.getValue();
			for (GroupAddress groupAddress : nodes) {
				GroupAddressBean bean = addressToBean.get(groupAddress);
				folder.buildDataPoint(lastNode, bean);
			}
		}
	}
}
