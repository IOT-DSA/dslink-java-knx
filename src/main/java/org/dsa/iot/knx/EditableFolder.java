package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EditableFolder {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(EditableFolder.class);
	}

	static final String ATTR_NAME = "name";
	static final String ATTR_POINT_TYPE = "type";
	static final String ATTR_RESTORE_TYPE = "restoreType";
	static final String ATTR_EDITABLE_FOLDER = "editable folder";

	static final String ACTION_REMOVE = "remove";
	static final String ACTION_EDIT = "edit";
	static final String ACTION_ADD_POINT = "add object";
	static final String ACTION_ADD_FOLDER = "add folder";

	KnxConnection conn;
	Node node;
	EditableFolder root;

	public EditableFolder(KnxConnection conn, Node node) {
		this.conn = conn;
		this.node = node;

		makeEditAction();
		makeRemoveAction();
		makeAddPointAction();
		makeAddFolderAction();
	}

	public EditableFolder(KnxConnection conn, EditableFolder root, Node node) {
		this(conn, node);

		this.root = root;
	}

	public void makeEditAction() {
		Action act;
		act = new Action(Permission.READ, new EditHandler());
		node.createChild(ACTION_EDIT).setAction(act).build().setSerializable(false);
	}

	public void makeRemoveAction() {
		Action act;
		act = new Action(Permission.READ, new RemoveHandler());
		node.createChild(ACTION_REMOVE).setAction(act).build().setSerializable(false);
	}

	public void makeAddPointAction() {
		Action act;
		act = new Action(Permission.READ, new AddPointHandler());
		act.addParameter(new Parameter(ATTR_NAME, ValueType.STRING));
		act.addParameter(new Parameter(ATTR_POINT_TYPE, ValueType.makeEnum(Utils.enumNames(PointType.class))));

		node.createChild(ACTION_ADD_POINT).setAction(act).build().setSerializable(false);
	}

	public void makeAddFolderAction() {
		Action act;
		act = new Action(Permission.READ, new AddFolderHandler());
		act.addParameter(new Parameter(ATTR_NAME, ValueType.STRING));
		node.createChild(ACTION_ADD_FOLDER).setAction(act).build().setSerializable(false);
	}

	protected class EditHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			edit(event);
		}
	}

	protected class RemoveHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			node.clearChildren();
			node.getParent().removeChild(node);
		}
	}

	protected class AddFolderHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			String name = event.getParameter(ATTR_NAME, ValueType.STRING).getString();
			addFolder(name);
		}
	}

	protected class AddPointHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			String name = event.getParameter(ATTR_NAME, ValueType.STRING).getString();
			PointType type;

			try {
				type = PointType
						.valueOf(event.getParameter(ATTR_POINT_TYPE, ValueType.STRING).getString().toUpperCase());
			} catch (Exception e) {
				LOGGER.error("invalid type");
				LOGGER.debug("error: ", e);
				return;
			}

			addPoint(name, type, event);
		}
	}

	void restoreLastSession() {
		if (node.getChildren() == null)
			return;

	}

	protected abstract void edit(ActionResult event);

	protected abstract void addPoint(String name, PointType type, ActionResult event);

	protected abstract void addFolder(String name);

}
