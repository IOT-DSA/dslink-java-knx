package org.dsa.iot.knx;

import java.util.Map;
import java.util.Queue;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.knx.masterdata.MasterDataParser;
import org.dsa.iot.knx.project.EtsXmlParser;
import org.dsa.iot.knx.project.KnxProjectParser;
import org.dsa.iot.knx.project.OpcFileParser;
import org.dsa.iot.knx.datapoint.DatapointType;
import org.dsa.iot.knx.groupaddress.GroupAddressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class DeviceFolder extends EditableFolder {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(DeviceFolder.class);
	}

	public DeviceFolder(KnxConnection conn, Node node) {
		super(conn, node);
	}

	public DeviceFolder(KnxConnection conn, EditableFolder root, Node node) {
		super(conn, node);

		this.root = root;
	}

	@Override
	protected void edit(ActionResult event) {

	}

	@Override
	protected void addFolder(String name) {
		Node child = node.createChild(name, true).build();
		new DeviceFolder(conn, root, child);
	}

	@Override
	protected void addPoint(ActionResult event) {
		String pointName = event.getParameter(ATTR_POINT_NAME, ValueType.STRING).getString();
		String groupAddressStr = event.getParameter(ATTR_GROUP_ADDRESS).getString();

		DatapointType type;
		try {
			type = DatapointType
					.valueOf(event.getParameter(ATTR_POINT_TYPE, ValueType.STRING).getString().toUpperCase());
		} catch (Exception e) {
			LOGGER.error("invalid type");
			LOGGER.debug("error: ", e);
			return;
		}

		GroupAddress groupAddress = null;
		try {
			groupAddress = new GroupAddress(groupAddressStr);
		} catch (KNXFormatException e1) {
			LOGGER.debug("", e1);
			return;
		}

		String group = Utils.getGroupName(groupAddress);

		ValueType valType = DatapointType.getValueType(type);
		Node pointNode = node.createChild(pointName, true).setValueType(valType).build();
		pointNode.setAttribute(ATTR_POINT_TYPE, new Value(type.toString()));
		pointNode.setAttribute(ATTR_GROUP_ADDRESS, new Value(groupAddress.toString()));

		DevicePoint point = new DevicePoint(conn, this, pointNode);
		setupPoint(point, groupAddress.toString(), group);
	}

	@Override
	protected void importMasterData(ActionResult event) {
		String masterDataContent = event.getParameter(ATTR_MASTER_DATA_CONTENT, ValueType.STRING).getString();
		MasterDataParser masterDataParser = new MasterDataParser();
		if (masterDataContent != null && !masterDataContent.isEmpty()) {
			masterDataParser.parse(masterDataContent);
		}
	}

	@Override
	protected void importProjectByXml(ActionResult event) {
		String contentProject = event.getParameter(ATTR_PROJECT_CONTENT_XML, ValueType.STRING).getString();
		EtsXmlParser projectParser = new EtsXmlParser(this);
		if (contentProject != null && !contentProject.isEmpty()) {
			projectParser.parse(contentProject);
		}

	}

	@Override
	protected void importProjectByEsf(ActionResult event) {
		String content = event.getParameter(ATTR_PROJECT_CONTENT_ESF, ValueType.STRING).getString();
		KnxProjectParser parser = new OpcFileParser(this);

		parser.parse(content);
	}

	@Override
	protected void importProjectByGroupAddress(ActionResult event) {
		String content = event.getParameter(ATTR_PROJECT_CONTENT_GROUP_ADDRESS, ValueType.STRING).getString();
		boolean withNamingConvention = event.getParameter(ATTR_PROJECT_NAMING_CONVENTION, ValueType.BOOL).getBool();
		GroupAddressParser parser = new GroupAddressParser(this);

		parser.parse(content, withNamingConvention);
	}

	public Node buildFolderTree(Node parent, Queue<String> path) {
		if (path.size() > 0) {
			String name = path.poll();
			Node child = parent.createChild(name, true).build();
			new DeviceFolder(getConnection(), child);
			Node node = buildFolderTree(child, path);
			return node;
		} else {
			return parent;
		}
	}

	@Override
	public void buildDataPoint(Node parent, GroupAddressBean addressBean) {
		GroupAddress groupAddress = null;
		try {
			groupAddress = new GroupAddress(addressBean.getGroupAddress());
		} catch (KNXFormatException e) {
			LOGGER.debug("", e);
		} finally {
			if (null == groupAddress)
				return;
		}

		DatapointType type = DatapointType.valueOf(addressBean.getDataPointType());
		ValueType valueType = DatapointType.getValueType(type);
		String dataPointName = addressBean.getDataPointName();
		Node pointNode = parent.createChild(dataPointName, true).setValueType(valueType).build();
		pointNode.setAttribute(ATTR_POINT_TYPE, new Value(type.name()));
		pointNode.setAttribute(ATTR_GROUP_ADDRESS, new Value(groupAddress.toString()));

		DevicePoint point = new DevicePoint(conn, this, pointNode);
		setupPoint(point, groupAddress.toString(), addressBean.getMiddleGroup());
	}
	
	void setupPoint(DevicePoint point, String groupAddressStr, String group) {
		getConnection().setupPointListener(point);
		getConnection().updateGroupToPoints(group, point, true);
		getConnection().updateAddressToPoint(groupAddressStr, point, true);
	}

	@Override
	void restoreLastSession() {
		Map<String, Node> children = node.getChildren();
		if (null == children)
			return;

		for (Node child : children.values()) {
			Value restype = child.getAttribute(ATTR_RESTORE_TYPE);
			if (null != restype && ATTR_EDITABLE_FOLDER.equals(restype.getString())) {
				DeviceFolder folder = new DeviceFolder(this.getConnection(), root, child);
				folder.restoreLastSession();
			} else if (null != restype && ATTR_EDITABLE_POINT.equals(restype.getString())) {
				DevicePoint point = new DevicePoint(this.getConnection(), this, child);
				try {
					String groupAddressStr = child.getAttribute(ATTR_GROUP_ADDRESS).getString();
					GroupAddress groupAddress = new GroupAddress(groupAddressStr);
					String group = Utils.getGroupName(groupAddress);
					setupPoint(point, groupAddressStr, group);
				} catch (Exception e) {
					LOGGER.debug("", e);
				}
			} else if (null == child.getAction() && !NODE_STATUS.equals(child.getName())) {
				node.removeChild(child);
			}
		}
	}
}
