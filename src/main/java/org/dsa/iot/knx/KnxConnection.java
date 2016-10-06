package org.dsa.iot.knx;

import java.util.List;
import java.util.Map;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.util.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KnxConnection {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(KnxConnection.class);
	}

	static final String ACTION_REMOVE = "remove";
	static final String ACTION_EDIT = "edit";

	KnxLink link;
	Node node;
	Map<String, List<EditablePoint>> groupToPoints;

	public KnxConnection(KnxLink link, Node node) {
		this.link = link;
		this.node = node;
	}

	public abstract void init();

	public void makeRemoveAction() {
		Action act = new Action(Permission.READ, new RemoveHandler());
		Node actionNode = node.getChild(ACTION_REMOVE);
		if (null == actionNode)
			node.createChild(ACTION_REMOVE).setAction(act).build().setSerializable(false);
		else
			actionNode.setAction(act);
	};

	private class RemoveHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			remove();
		}
	}

	public void remove() {
		node.clearChildren();
		node.getParent().removeChild(node);
	}

	public abstract void onDiscovered();

	public KnxLink getLink() {
		return this.link;
	}

	public void stopPolling(DevicePoint point) {

	}

	public void startPolling(DevicePoint point) {

	}

	public Map<String, List<EditablePoint>> getGroupToPoints() {
		return this.groupToPoints;
	}

	public void setPointValue(EditablePoint point, Value newVal) {
	}

	public void setupPointListener(DevicePoint devicePoint) {

	}

	public GroupAddressType getGroupLevel() {
		return GroupAddressType.THREE_LEVEL;
	}

	public void updateGroupToPoints(String group, DevicePoint point, boolean add) {
	}

	public void updateAddressToPoint(String address, EditablePoint point, boolean add) {
	}

}
