package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
			type = PointType
					.valueOf(event.getParameter(ATTR_POINT_TYPE, ValueType.STRING).getString().toUpperCase());
		} catch (Exception e) {
			LOGGER.error("invalid type");
			LOGGER.debug("error: ", e);
			return;
		}

		String name = mainGroupName + "_" + middleGroupName + "_" + subGroupName;
		Node pointNode = node.createChild(name).setValueType(ValueType.STRING).build();

		pointNode.setAttribute(ATTR_POINT_TYPE, new Value(type.toString()));
		pointNode.setAttribute(ATTR_MAIN_GROUP_NAME, new Value(mainGroupName));
		pointNode.setAttribute(ATTR_MIDDLE_GROUP_NAME, new Value(middleGroupName));
		pointNode.setAttribute(ATTR_SUB_GROUP_NAME, new Value(subGroupName));
		pointNode.setAttribute(ATTR_MAIN_GROUP_ADDRESS, new Value(mainGroupAddress));
		pointNode.setAttribute(ATTR_MIDDLE_GROUP_ADDRESS, new Value(middleGroupAddress));
		pointNode.setAttribute(ATTR_SUB_GROUP_ADDRESS, new Value(subGroupAddress));
		pointNode.setAttribute(ATTR_RESTORE_TYPE, new Value(ATTR_EDITBALE_POINT));

		DevicePoint knxPoint = new DevicePoint(conn, this, pointNode);
		this.getConnection().getGroupToPoint().put(middleGroupName, knxPoint);
	}

}
