package org.dsa.iot.knx;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.knx.project.OpcFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class DeviceFolder extends EditableFolder {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(DeviceFolder.class);
	}

	static final String ATTR_POINT_TYPE = "point type";
	static final String ATTR_EDITBALE_POINT = "editable point";

	DeviceFolder root;

	public DeviceFolder(KnxConnection conn, Node node) {
		super(conn, node);
	}

	public DeviceFolder(KnxConnection conn, DeviceFolder root, Node node) {
		super(conn, node);

		this.root = root;
	}

	public DeviceFolder getRoot() {
		return this.root;
	}

	@Override
	protected void edit(ActionResult event) {

	}

	@Override
	protected void addFolder(String name) {
		Node child = node.createChild(name).build();
		new DeviceFolder(conn, root, child);
	}

	@Override
	protected void addPoint(ActionResult event) {
		String mainGroupName = event.getParameter(ATTR_MAIN_GROUP_NAME, ValueType.STRING).getString();
		String middleGroupName = event.getParameter(ATTR_MIDDLE_GROUP_NAME, ValueType.STRING).getString();
		String subGroupName = event.getParameter(ATTR_SUB_GROUP_NAME, ValueType.STRING).getString();

		int mainGroupAddress = event.getParameter(ATTR_MAIN_GROUP_ADDRESS, ValueType.NUMBER).getNumber().intValue();
		int middleGroupAddress = event.getParameter(ATTR_MIDDLE_GROUP_ADDRESS, ValueType.NUMBER).getNumber().intValue();
		int subGroupAddress = event.getParameter(ATTR_SUB_GROUP_ADDRESS, ValueType.NUMBER).getNumber().intValue();

		PointType type;
		try {
			type = PointType.valueOf(event.getParameter(ATTR_POINT_TYPE, ValueType.STRING).getString().toUpperCase());
		} catch (Exception e) {
			LOGGER.error("invalid type");
			LOGGER.debug("error: ", e);
			return;
		}

		String name = mainGroupName + "_" + middleGroupName + "_" + subGroupName;
		Node pointNode = node.createChild(name).setValueType(ValueType.BOOL).build();

		pointNode.setAttribute(ATTR_POINT_TYPE, new Value(type.toString()));
		pointNode.setAttribute(ATTR_MAIN_GROUP_NAME, new Value(mainGroupName));
		pointNode.setAttribute(ATTR_MIDDLE_GROUP_NAME, new Value(middleGroupName));
		pointNode.setAttribute(ATTR_SUB_GROUP_NAME, new Value(subGroupName));
		pointNode.setAttribute(ATTR_MAIN_GROUP_ADDRESS, new Value(mainGroupAddress));
		pointNode.setAttribute(ATTR_MIDDLE_GROUP_ADDRESS, new Value(middleGroupAddress));
		pointNode.setAttribute(ATTR_SUB_GROUP_ADDRESS, new Value(subGroupAddress));
		pointNode.setAttribute(ATTR_RESTORE_TYPE, new Value(ATTR_EDITBALE_POINT));

		DevicePoint knxPoint = new DevicePoint(conn, this, pointNode);
		getConnection().updateGroupToPoints(middleGroupName, knxPoint);
	}

	@Override
	protected void importProjectByXml(ActionResult event) {

	}

	@Override
	protected void importProjectByEsf(ActionResult event) {
		String content = event.getParameter(ATTR_PROJECT_CONTENT, ValueType.STRING).getString();
		OpcFileParser parser = new OpcFileParser(this, content);

		parser.parseItems();
	}

	private ValueType getValueType(PointType type) {
		ValueType vt = ValueType.STRING;

		if (type == PointType.BOOL) {
			vt = ValueType.BOOL;
		} else if (type == PointType.CONTROL) {
			vt = ValueType.BOOL;
		} else if (type == PointType.FLOAT2 || type == PointType.FLOAT4) {
			vt = ValueType.NUMBER;
		} else if (type == PointType.UNSIGNED) {
			vt = ValueType.NUMBER;
		} else if (type == PointType.STRING) {
			vt = ValueType.STRING;
		}

		return vt;
	}

	public Node buildFolderTree(Node parent, Queue<String> path) {
		if (path.size() > 0) {
			String name = path.poll();
			Node child = parent.createChild(name).build();
			KnxConnection conn = getConnection();
			DeviceFolder folder = new DeviceFolder(conn, child);
			LOGGER.info(folder.node.getName());
			Node node = buildFolderTree(child, path);
			return node;
		} else {
			return parent;
		}
	}

	@Override
	public void buildDataPoint(Node parent, GroupAddressBean addressBean) {
		GroupAddress address = null;
		try {
			address = new GroupAddress(addressBean.getGroupAddress());
		} catch (KNXFormatException e) {
			e.printStackTrace();
		}
		int mainGroupAddress = address.getMainGroup();
		int middleGroupAddress = address.getMiddleGroup();
		GroupAddressType groupLevel = getConnection().getGroupLevel();
		int subGroupAddress = 0;
		if (groupLevel == GroupAddressType.THREE_LEVEL) {
			subGroupAddress = address.getSubGroup8();
		} else if (groupLevel == GroupAddressType.TWO_LEVEL) {
			subGroupAddress = address.getSubGroup11();
		}

		PointType type = PointType.getDataTypeByDataSize(addressBean.getDataSize());
		ValueType valueType = getValueType(type);

		Node pointNode = parent.createChild(addressBean.getName()).setValueType(valueType).build();
		pointNode.setAttribute(ATTR_POINT_TYPE, new Value(type.toString()));
		pointNode.setAttribute(ATTR_MAIN_GROUP_ADDRESS, new Value(mainGroupAddress));
		pointNode.setAttribute(ATTR_MIDDLE_GROUP_ADDRESS, new Value(middleGroupAddress));
		pointNode.setAttribute(ATTR_SUB_GROUP_ADDRESS, new Value(subGroupAddress));
		pointNode.setAttribute(ATTR_RESTORE_TYPE, new Value(ATTR_EDITBALE_POINT));

		DevicePoint knxPoint = new DevicePoint(conn, this, pointNode);
		getConnection().updateGroupToPoints(addressBean.getMiddleGroup(), knxPoint);
	}

}
