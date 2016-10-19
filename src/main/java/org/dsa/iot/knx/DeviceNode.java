package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.knxnetip.util.DeviceDIB;

public class DeviceNode extends DeviceFolder {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(DeviceNode.class);
	}

	public static final String ATTR_INDIVIDUAL_ADDRESS = "individual address";
	public static final String ATTR_MEDIUM = "medium";
	public static final String ATTR_MAC_ADDRESS = "mac address";
	public static final String NODE_STATUS = "STATUS";

	Node statusNode;

	public DeviceNode(KnxConnection conn, DeviceFolder root, Node node) {
		super(conn, node);

		this.root = this;
		if (null != node.getChild(NODE_STATUS)) {
			this.statusNode = node.getChild(NODE_STATUS);

		} else {
			this.statusNode = node.createChild(NODE_STATUS).setValueType(ValueType.STRING)
					.setValue(new Value("enabled")).build();
		}
	}

	public DeviceNode(KnxConnection conn, DeviceFolder root, Node node, DeviceDIB dib) {
		this(conn, root, node);

		IndividualAddress address = dib.getAddress();
		String mediumStr = dib.getKNXMediumString();
		String macStr = dib.getMACAddressString();

		node.setAttribute(ATTR_INDIVIDUAL_ADDRESS, new Value(address.toString()));
		node.setAttribute(ATTR_MEDIUM, new Value(mediumStr));
		node.setAttribute(ATTR_MAC_ADDRESS, new Value(macStr));
	}
}
