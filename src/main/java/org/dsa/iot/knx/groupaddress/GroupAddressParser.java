
package org.dsa.iot.knx.groupaddress;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.knx.EditableFolder;
import org.dsa.iot.knx.GroupAddressBean;
import org.dsa.iot.knx.datapoint.DatapointType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
			LOGGER.debug(e.getMessage());
		}
	}

	public void buildGroupTree() {
		Set<Map.Entry<String, List<String>>> entrySet = pathToNodes.entrySet();
		for (Map.Entry entry : entrySet) {
			String path = (String) entry.getKey();
			String[] subDirectories = path.split("_");
			Queue<String> queue = new LinkedList<>(Arrays.asList(subDirectories));
			Node lastNode = folder.buildFolderTree(folder.getNode(), queue);

			List<String> nodes = (ArrayList<String>) entry.getValue();
			for (String address : nodes) {
				GroupAddressBean bean = addressToBean.get(address);
				folder.buildDataPoint(lastNode, bean);
			}
		}
	}

	public String[] parseGroupAddressName(String groupAddressName) {
		String[] nameArray = groupAddressName.split("_");
		// data point follows the naming convention: command_senario_state
		String dataPointName = nameArray[0] + "_" + nameArray[1] + "_" + nameArray[2];
		String pathAndAnotation = null;
		// remove the annotation symbol
		if (groupAddressName.endsWith("@")) {
			pathAndAnotation = groupAddressName.substring(dataPointName.length() + 1, groupAddressName.length() - 1);
		} else {
			pathAndAnotation = groupAddressName.substring(dataPointName.length() + 1);
		}

		String path = pathAndAnotation;
		if (pathAndAnotation.contains(" ")) {
			path = pathAndAnotation.substring(0, pathAndAnotation.indexOf(' '));
		}
		return new String[] { dataPointName, path };
	}
}
