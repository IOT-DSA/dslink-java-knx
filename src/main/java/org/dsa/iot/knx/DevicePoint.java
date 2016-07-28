package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.value.Value;

public class DevicePoint extends EditablePoint {
	public DevicePoint(KnxConnection conn, EditableFolder folder, Node node) {
		super(conn, folder, node);
	}

	@Override
	protected void handleSet(Value val) {

	}
}
