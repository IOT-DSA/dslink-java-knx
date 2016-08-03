package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;

public class DevicePoint extends EditablePoint {
	private static final Logger LOGGER;
	
	static {
		LOGGER = LoggerFactory.getLogger(DevicePoint.class);
	}
	
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
		return this.groupAddress;
	}
}
