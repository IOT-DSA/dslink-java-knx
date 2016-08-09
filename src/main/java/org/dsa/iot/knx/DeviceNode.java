package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceNode extends DeviceFolder {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(DeviceNode.class);
	}

	public DeviceNode(KnxConnection conn, DeviceFolder root, Node node) {
		super(conn, root, node);
	}

}
