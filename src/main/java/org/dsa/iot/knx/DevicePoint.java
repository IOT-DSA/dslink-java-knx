package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.knx.datapoint.DPT;
import org.dsa.iot.knx.datapoint.DatapointType;
import org.dsa.iot.knx.datapoint.DatapointUnit;
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

	}

	@Override
	protected void handleSet(Value newVal) {
		folder.getConnection().setPointValue(this, newVal);
	}

	@Override
	public DatapointType getType() {
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
		ValueType valType;
		try {
			type = DatapointType
					.valueOf(event.getParameter(ATTR_POINT_TYPE, ValueType.STRING).getString().toUpperCase());
			valType = DatapointType.getValueType(type);
		} catch (Exception e) {
			LOGGER.debug("error: ", e);
			return;
		}

		String groupAddressStr = event.getParameter(ATTR_GROUP_ADDRESS).getString();
		GroupAddress groupAddress = null;
		try {
			groupAddress = new GroupAddress(groupAddressStr);
		} catch (KNXFormatException e) {
			LOGGER.debug(e.getMessage());
			return;
		}
   
        String group = Utils.getGroupName(groupAddress);
		remove(null);
		getConnection().updateGroupToPoints(group, this, true);
		getConnection().updateAddressToPoint(groupAddress.toString(), this, true);

		String name = node.getName();
		String newname = event.getParameter(ATTR_NAME, ValueType.STRING).getString();
		if (null != newname && !newname.isEmpty() && !newname.equals(name)) {
			Node parent = node.getParent();
			parent.removeChild(node);

			node = parent.createChild(newname).setValueType(valType).build();
			this.node.setAttribute(ATTR_RESTORE_TYPE, new Value(RESTORE_EDITABLE_POINT));
			makeRemoveAction();
			makeSetAction();

			getConnection().setupPointListener(this);
		}

		node.setValueType(valType);
		node.setAttribute(ATTR_POINT_TYPE, new Value(type.toString()));
		node.setAttribute(ATTR_GROUP_ADDRESS, new Value(groupAddress.toString()));
		DPT dpt = type.getDpt();
		if (dpt instanceof DatapointUnit) {
			node.setAttribute(ATTR_UNIT, new Value(((DatapointUnit) dpt).getUnit()));
		}
	}

	@Override
	public void remove(ActionResult event) {
		String address = node.getAttribute(ATTR_GROUP_ADDRESS).getString();
		GroupAddress groupAddress = null;
		try {
			groupAddress = new GroupAddress(address);
		} catch (KNXFormatException e1) {
			e1.printStackTrace();
			return;
		}

        String group = Utils.getGroupName(groupAddress);
		this.conn.updateGroupToPoints(group, this, false);
		this.conn.updateAddressToPoint(address, this, false);

	}
}
