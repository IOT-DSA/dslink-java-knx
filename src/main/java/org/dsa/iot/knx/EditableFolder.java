package org.dsa.iot.knx;

import java.util.Queue;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.EditorType;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;

public abstract class EditableFolder {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(EditableFolder.class);
	}

	static final String ATTR_NAME = "name";
	static final String ATTR_MAIN_GROUP_NAME = "main group name";
	static final String ATTR_MIDDLE_GROUP_NAME = "middle group name";
	static final String ATTR_SUB_GROUP_NAME = "sub group name";
	static final String ATTR_MAIN_GROUP_ADDRESS = "main group address";
	static final String ATTR_MIDDLE_GROUP_ADDRESS = "middle group address";
	static final String ATTR_SUB_GROUP_ADDRESS = "sub group address";
	static final String ATTR_POINT_TYPE = "point type";
	static final String ATTR_RESTORE_TYPE = "restoreType";
	static final String ATTR_EDITABLE_FOLDER = "editable folder";
	static final String ATTR_PROJECT_CONTENT = "project content";

	static final String ACTION_REMOVE = "remove";
	static final String ACTION_EDIT = "edit";
	static final String ACTION_ADD_POINT = "add datapoint";
	static final String ACTION_ADD_FOLDER = "add folder";
	static final String ACTION_IMPORT_PROJECT = "import by xml";
	static final String ACTION_IMPORT_OPC = "import by esf";

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
		makeImportProjectAction();
		makeImportOpcAction();
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
		act.addParameter(new Parameter(ATTR_POINT_TYPE, ValueType.makeEnum(Utils.enumNames(PointType.class))));
		act.addParameter(new Parameter(ATTR_MAIN_GROUP_NAME, ValueType.STRING));
		act.addParameter(new Parameter(ATTR_MIDDLE_GROUP_NAME, ValueType.STRING));
		act.addParameter(new Parameter(ATTR_SUB_GROUP_NAME, ValueType.STRING));
		act.addParameter(new Parameter(ATTR_MAIN_GROUP_ADDRESS, ValueType.NUMBER));
		act.addParameter(new Parameter(ATTR_MIDDLE_GROUP_ADDRESS, ValueType.NUMBER));
		act.addParameter(new Parameter(ATTR_SUB_GROUP_ADDRESS, ValueType.NUMBER));
		
		node.createChild(ACTION_ADD_POINT).setAction(act).build().setSerializable(false);
	}

	public void makeAddFolderAction() {
		Action act;
		act = new Action(Permission.READ, new AddFolderHandler());
		act.addParameter(new Parameter(ATTR_NAME, ValueType.STRING));
		node.createChild(ACTION_ADD_FOLDER).setAction(act).build().setSerializable(false);
	}

	public void makeImportProjectAction() {
		Action act;
		act = new Action(Permission.READ, new AddImportProjectHandler());
		act.addParameter(new Parameter(ATTR_PROJECT_CONTENT, ValueType.STRING).setEditorType(EditorType.TEXT_AREA));

		node.createChild(ACTION_IMPORT_PROJECT).setAction(act).build().setSerializable(false);
	}

	public void makeImportOpcAction() {
		Action act;
		act = new Action(Permission.READ, new AddImportOpcHandler());
		act.addParameter(new Parameter(ATTR_PROJECT_CONTENT, ValueType.STRING).setEditorType(EditorType.TEXT_AREA));

		node.createChild(ACTION_IMPORT_OPC).setAction(act).build().setSerializable(false);
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
			addPoint(event);
		}
	}

	protected class AddImportProjectHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			importProjectByXml(event);
		}
	}

	protected class AddImportOpcHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			importProjectByEsf(event);
		}
	}

	void restoreLastSession() {
		if (node.getChildren() == null)
			return;

	}

	public KnxConnection getConnection() {
		return this.conn;
	}

	public Node getNode() {
		return node;
	}

	protected abstract void edit(ActionResult event);

	protected abstract void addPoint(ActionResult event);

	protected abstract void addFolder(String name);

	protected abstract void importProjectByXml(ActionResult event);

	protected abstract void importProjectByEsf(ActionResult event);

	public void buildDataPoint(Node parent, GroupAddressBean addressBean) {

	}

	public Node buildFolderTree(Node node2, Queue<String> queue) {
		return null;
	}
}
