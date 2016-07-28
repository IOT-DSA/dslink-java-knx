package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceFolder extends EditableFolder {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(EditableFolder.class);
	}

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
	protected void addPoint(String name, PointType pointType, ActionResult event) {
		Node pointNode = node.createChild(name).build();
		pointNode.setAttribute("point type", new Value(PointType.BOOL.toString()));
		pointNode.setAttribute("restore type", new Value("editable point"));

		DevicePoint knxPoint = new DevicePoint(conn, this, pointNode);
	}
}
