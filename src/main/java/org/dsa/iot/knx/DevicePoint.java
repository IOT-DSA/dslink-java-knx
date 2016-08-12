package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
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
		String addressStr = node.getAttribute(ATTR_INDIVIDUAL_ADDRESS).getString();
		IndividualAddress address = null;
		try {
			address = new IndividualAddress(addressStr);
		} catch (KNXFormatException e) {
			e.printStackTrace();
		} finally {
			if (null == address) {
				return null;
			}
		}

		int area = address.getArea();
		int line = address.getLine();
		int device = address.getDevice();
		return new GroupAddress(area, line, device);
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
}
