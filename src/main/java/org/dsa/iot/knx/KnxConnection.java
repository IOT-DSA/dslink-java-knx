package org.dsa.iot.knx;

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
	Map<String, EditablePoint> groupToPoint;

	public KnxConnection(KnxLink link, Node node) {
		this.link = link;
		this.node = node;
	}

	public abstract void init();

	public void makeRemoveAction() {
		Action act = new Action(Permission.READ, new RemoveHandler());
		Node actionNode = node.getChild(ACTION_EDIT);
		if (actionNode == null)
			node.createChild(ACTION_EDIT).setAction(act).build().setSerializable(false);
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

	public void stopPolling() {
		
	}

	public void startPolling() {
	
	}

	public Map<String, EditablePoint> getGroupToPoint() {
		return this.groupToPoint;
	}

	public void setPointValue(EditablePoint point, Value newVal) {
	}

	public void setupPointListener(DevicePoint devicePoint) {

	}

}
