package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.util.handler.Handler;

public abstract class KnxConnection {
	KnxLink link;
	Node node;

	public KnxConnection(KnxLink link, Node node) {
		this.link = link;
		this.node = node;
	}

	public abstract void init();

	public void makeEditAction() {
	};

	public void makeRemoveAction() {
		Action act = new Action(Permission.READ, new RemoveHandler());
		Node actionNode = node.getChild("remove");
		if (actionNode == null)
			node.createChild("remove").setAction(act).build().setSerializable(false);
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

}
