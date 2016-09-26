package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

public class DevicePoint extends EditablePoint {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(DevicePoint.class);
	}

	boolean isSubscribed;

	public DevicePoint(KnxConnection conn, EditableFolder folder, Node node) {
		super(conn, folder, node);

		folder.getConnection().setupPointListener(this);
	}

	@Override
	protected void handleSet(Value newVal) {
		folder.getConnection().setPointValue(this, newVal);
	}

	@Override
	public PointType getType() {
		return this.type;
	}

	@Override
	public GroupAddress getGroupAddress() {
		String addressStr = node.getAttribute(ATTR_GROUP_ADDRESS).getString();
		GroupAddress groupAddress = null;
		try {
			groupAddress = new GroupAddress(addressStr);
		} catch (KNXFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return groupAddress;
	}

	public void startPolling() {
		isSubscribed = true;
		getConnection().startPolling(this);
	}

	public void stopPolling() {
		isSubscribed = false;
		getConnection().stopPolling(this);
	}

	@Override
	public boolean isSubscribed() {
		return this.isSubscribed;
	}

	@Override
	public void edit(ActionResult event) {
		PointType type;
		ValueType valType;
		try {
			type = PointType.valueOf(event.getParameter(ATTR_POINT_TYPE, ValueType.STRING).getString().toUpperCase());
			valType = PointType.getValueType(type);
		} catch (Exception e) {
			LOGGER.debug("error: ", e);
			return;
		}

		String newname = event.getParameter(ATTR_NAME, ValueType.STRING).getString();
		if (null != newname && !newname.isEmpty() && !newname.equals(node.getName())
				|| null != type && !type.toString().equals(node.getAttribute(ATTR_POINT_TYPE))) {
			Node parent = node.getParent();
			parent.removeChild(node);
			node = parent.createChild(newname).setValueType(valType).build();
		}

		String groupAddressStr = event.getParameter(ATTR_GROUP_ADDRESS).getString();
		GroupAddress groupAddress = null;
		try {
			groupAddress = new GroupAddress(groupAddressStr);
		} catch (KNXFormatException e) {
			LOGGER.debug(e.getMessage());
			return;
		}

		node.setAttribute(ATTR_POINT_TYPE, new Value(type.toString()));
		node.setAttribute(ATTR_GROUP_ADDRESS, new Value(groupAddress.toString()));
		if (PointType.UNSIGNED.equals(type)) {
			node.setAttribute(ATTR_UNIT, new Value(PERCENTAGE_UNIT));
		}
	}
}
