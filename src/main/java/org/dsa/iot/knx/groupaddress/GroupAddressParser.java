/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dsa.iot.knx.groupaddress;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.knx.EditableFolder;
import org.dsa.iot.knx.GroupAddressBean;
import org.dsa.iot.knx.datapoint.DatapointType;
import org.dsa.iot.knx.project.KnxProjectParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author hua hou
 */
public class GroupAddressParser {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(GroupAddressParser.class);
	}

	EditableFolder folder;
	Map<String, GroupAddressBean> addressToBean = new HashMap<String, GroupAddressBean>();
	Map<String, List<String>> pathToNodes = new HashMap<>();

	public GroupAddressParser(EditableFolder folder) {
		this.folder = folder;
	}

	public void parse(String content) {
		JAXBContext context = null;
		try {
			context = JAXBContext.newInstance(GroupAddressExport.class);
			Unmarshaller unMarshaller = context.createUnmarshaller();
			InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			GroupAddressExport export = (GroupAddressExport) unMarshaller.unmarshal(stream);
			List<GroupRange> majorGroupRangeList = export.getGroupRange();
			for (GroupRange major : majorGroupRangeList) {
				List<GroupRange> middleGroupRangeList = major.getGroupRange();
				for (GroupRange middle : middleGroupRangeList) {
					List<GroupRange.GroupAddress> groupAddressList = middle.getGroupAddress();
					for (GroupRange.GroupAddress groupAddress : groupAddressList) {
						String address = groupAddress.getAddress();
						String dptId = groupAddress.getDPTs();
						DatapointType type = DatapointType.forTypeId(dptId);
						String nameWithPath = groupAddress.getName();
						String[] nameArray = parseGroupAddressName(nameWithPath);
						String name = nameArray[0];
						String path = nameArray[1];
						
						GroupAddressBean bean = new GroupAddressBean();
						bean.setDptId(dptId);
						bean.setGroupAddress(address);
						bean.setDataPointName(name);
						bean.setDataPointType(type.name());
						addressToBean.put(address, bean);

						if (!pathToNodes.containsKey(path)) {
							List<String> nodes = new ArrayList<>();
							nodes.add(address);
							pathToNodes.put(path, nodes);
						} else {
							List<String> nodes = pathToNodes.get(path);
							nodes.add(address);
							pathToNodes.put(path, nodes);
						}
						LOGGER.info(address + " : " + dptId + " : " + name + " : " + path);
					}
				}
			}
			// build the folder tree from the hashMap
			buildGroupTree();
		} catch (Exception e) {

		}
	}

	public void buildGroupTree() {
		Iterator it = pathToNodes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String path = (String) pair.getKey();
			String[] subDirectories = path.split("_");
			Queue<String> queue = new LinkedList<>(Arrays.asList(subDirectories));
			Node lastNode = folder.buildFolderTree(folder.getNode(), queue);

			List<String> nodes = (ArrayList<String>) pair.getValue();
			for (String address : nodes) {
				GroupAddressBean bean = addressToBean.get(address);
				folder.buildDataPoint(lastNode, bean);
			}
		}
	}

	public String[] parseGroupAddressName(String groupAddressName) {
		String[] nameArray = groupAddressName.split("_");
		String dataPointName = nameArray[0] + "_" + nameArray[1] + "_" + nameArray[2];
		String pathAndAnotation = groupAddressName.substring(dataPointName.length() + 1, groupAddressName.length() - 1);
		int index = pathAndAnotation.indexOf(' ');
		String path = pathAndAnotation;
		if (index != -1) {
			path = pathAndAnotation.substring(0, index);
		}
		return new String[] { dataPointName, path };
	}
}
