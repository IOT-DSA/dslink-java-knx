package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValuePair;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;

public abstract class EditablePoint {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(EditablePoint.class);
	}

	static final String ATTR_NAME = "name";
	static final String ATTR_POINT_TYPE = "point type";
	static final String ATTR_MAIN_GROUP_NAME = "main group name";
	static final String ATTR_MIDDLE_GROUP_NAME = "middle group name";
	static final String ATTR_SUB_GROUP_NAME = "sub group name";
	static final String ATTR_MAIN_GROUP_ADDRESS = "main group address";
	static final String ATTR_MIDDLE_GROUP_ADDRESS = "middle group address";
	static final String ATTR_SUB_GROUP_ADDRESS = "sub group address";
	static final String ATTR_INDIVIDUAL_ADDRESS = "individual address";
	static final String ATTR_RESTORE_TYPE = "restore type";
	static final String RESTORE_EDITABLE_POINT = "editable point";

	static final String ACTION_REMOVE = "remove";
	static final String ACTION_EDIT = "edit";

	static final String DEFAULT_GROUP_ADDRESS = "0";

	KnxConnection conn;
	EditableFolder folder;
	Node node;

	ValueType valType;
	PointType type;

	public EditablePoint(KnxConnection conn, EditableFolder folder, Node node) {
		this.conn = conn;
		this.folder = folder;
		this.node = node;
		this.node.setAttribute(ATTR_RESTORE_TYPE, new Value(RESTORE_EDITABLE_POINT));

		makeEditAction();
		makeRemoveAction();
		makeSetAction();

		this.type = PointType.parseType(node.getAttribute(ATTR_POINT_TYPE).getString());
	}

	protected void makeEditAction() {

		Action act = new Action(Permission.READ, new EditHandler());
		act.addParameter(new Parameter(ATTR_NAME, ValueType.STRING, new Value(node.getName())));
		act.addParameter(new Parameter(ATTR_POINT_TYPE, ValueType.makeEnum(Utils.enumNames(PointType.class)),
				node.getAttribute(ATTR_POINT_TYPE)));
		act.addParameter(new Parameter(ATTR_MAIN_GROUP_NAME, ValueType.STRING, new Value("")));
		act.addParameter(new Parameter(ATTR_MIDDLE_GROUP_NAME, ValueType.STRING, new Value("")));
		act.addParameter(new Parameter(ATTR_SUB_GROUP_NAME, ValueType.STRING, new Value(DEFAULT_GROUP_ADDRESS)));
		act.addParameter(new Parameter(ATTR_MAIN_GROUP_ADDRESS, ValueType.STRING, new Value(DEFAULT_GROUP_ADDRESS)));
		act.addParameter(new Parameter(ATTR_MIDDLE_GROUP_ADDRESS, ValueType.STRING, new Value(DEFAULT_GROUP_ADDRESS)));
		act.addParameter(new Parameter(ATTR_SUB_GROUP_ADDRESS, ValueType.STRING, new Value(DEFAULT_GROUP_ADDRESS)));

		Node actionNode = node.getChild(ACTION_EDIT);
		if (actionNode == null)
			node.createChild(ACTION_EDIT).setAction(act).build().setSerializable(false);
		else
			actionNode.setAction(act);
	}

	public void makeRemoveAction() {
		Action act = new Action(Permission.READ, new RemoveHandler());
		Node actionNode = node.getChild(ACTION_REMOVE);
		if (actionNode == null)
			node.createChild(ACTION_REMOVE).setAction(act).build().setSerializable(false);
		else
			actionNode.setAction(act);
	}

	protected class EditHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			String newname = event.getParameter("name", ValueType.STRING).getString();
			if (newname != null && !newname.isEmpty() && !newname.equals(node.getName())) {
				Node parent = node.getParent();
				parent.removeChild(node);
				node = parent.createChild(newname).build();
			}

			makeEditAction();
		}
	}

	protected class RemoveHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			node.clearChildren();
			node.getParent().removeChild(node);
		}
	}

	protected void makeSetAction() {
		node.setWritable(Writable.WRITE);
		node.getListener().setValueHandler(new setValueHandler());
	}

	private class setValueHandler implements Handler<ValuePair> {

		public void handle(ValuePair event) {
			if (!event.isFromExternalSource()) {
				return;
			}
			Value newVal = event.getCurrent();
			handleSet(newVal);
		}
	}

	protected KnxConnection getConnection() {
		return this.conn;
	}

	protected abstract void handleSet(Value val);

	public abstract PointType getType();

	public abstract GroupAddress getGroupAddress();

	public boolean isSubscribed() {
		return false;
	}

}
