package org.dsa.iot.knx;

import java.util.Queue;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.EditorType;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;
import org.dsa.iot.knx.datapoint.DatapointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EditableFolder {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(EditableFolder.class);
	}

	static final String ATTR_NAME = "name";
	static final String ATTR_POINT_NAME = "point name";
	static final String ATTR_GROUP_ADDRESS = "group address";
	static final String ATTR_POINT_TYPE = "point type";
	static final String ATTR_RESTORE_TYPE = "restore type";
	static final String ATTR_EDITABLE_FOLDER = "editable folder";
	static final String ATTR_EDITABLE_POINT = "editable point";
	static final String ATTR_PROJECT_CONTENT_XML = "xml content";
	static final String ATTR_PROJECT_CONTENT_ESF = "esf content";
	static final String ATTR_PROJECT_CONTENT_GROUP_ADDRESS = "group address content";
	static final String ATTR_PROJECT_NAMING_CONVENTION = "naming convention";
	static final String ATTR_MASTER_DATA_CONTENT = "master data content";
	static final String ATTR_UNIT = "unit";
	static final String ACTION_REMOVE = "remove";
	static final String ACTION_EDIT = "edit";
	static final String ACTION_ADD_POINT = "add group address";
	static final String ACTION_ADD_FOLDER = "add folder";
	static final String ACTION_IMPORT_MASTER_DATA = "import master data";
	static final String ACTION_IMPORT_PROJECT = "import project by xml";
	static final String ACTION_IMPORT_OPC = "import project by esf";
	static final String ACTION_IMPORT_GROUP_ADDRESS = "import project by group address";
	static final String NODE_STATUS = "STATUS";
	static final String DEFAULT_GROUP_ADDRESS = "0/0/0";
	static final String GROUP_ADDRESS_SEPARATOR = "/";

	KnxConnection conn;
	Node node;
	EditableFolder root;

	public EditableFolder(KnxConnection conn, Node node) {
		this.conn = conn;
		this.node = node;
		this.node.setAttribute(ATTR_RESTORE_TYPE, new Value(ATTR_EDITABLE_FOLDER));

		makeEditAction();
		makeRemoveAction();
		makeAddPointAction();
		makeAddFolderAction();
		makeImportMasterDataAction();
		makeImportProjectAction();
		makeImportOpcAction();
		makeImportGroupAddressAction();
	}

	public EditableFolder(KnxConnection conn, EditableFolder root, Node node) {
		this(conn, node);

		this.root = root;
	}

	public void makeEditAction() {
		Action act;
		act = new Action(Permission.READ, new EditHandler());
		node.createChild(ACTION_EDIT, true).setAction(act).build().setSerializable(false);
	}

	public void makeRemoveAction() {
		Action act;
		act = new Action(Permission.READ, new RemoveHandler());
		node.createChild(ACTION_REMOVE, true).setAction(act).build().setSerializable(false);
	}

	public void makeAddPointAction() {
		Action act;
		act = new Action(Permission.READ, new AddPointHandler());
		act.addParameter(new Parameter(ATTR_POINT_TYPE, ValueType.makeEnum(Utils.enumNames(DatapointType.class))));
		act.addParameter(new Parameter(ATTR_POINT_NAME, ValueType.STRING));
		act.addParameter(new Parameter(ATTR_GROUP_ADDRESS, ValueType.STRING, new Value(DEFAULT_GROUP_ADDRESS)));

		node.createChild(ACTION_ADD_POINT, true).setAction(act).build().setSerializable(false);
	}

	public void makeAddFolderAction() {
		Action act;
		act = new Action(Permission.READ, new AddFolderHandler());
		act.addParameter(new Parameter(ATTR_NAME, ValueType.STRING));
		node.createChild(ACTION_ADD_FOLDER, true).setAction(act).build().setSerializable(false);
	}

	public void makeImportMasterDataAction() {
		Action act;
		act = new Action(Permission.READ, new AddImportMasterDataHandler());
		act.addParameter(new Parameter(ATTR_MASTER_DATA_CONTENT, ValueType.STRING).setEditorType(EditorType.TEXT_AREA));

		node.createChild(ACTION_IMPORT_MASTER_DATA, true).setAction(act).build().setSerializable(false);
	}

	public void makeImportProjectAction() {
		Action act;
		act = new Action(Permission.READ, new AddImportProjectHandler());
		act.addParameter(new Parameter(ATTR_PROJECT_CONTENT_XML, ValueType.STRING).setEditorType(EditorType.TEXT_AREA));

		node.createChild(ACTION_IMPORT_PROJECT, true).setAction(act).build().setSerializable(false);
	}

	public void makeImportOpcAction() {
		Action act;
		act = new Action(Permission.READ, new AddImportOpcHandler());
		act.addParameter(new Parameter(ATTR_PROJECT_CONTENT_ESF, ValueType.STRING).setEditorType(EditorType.TEXT_AREA));

		node.createChild(ACTION_IMPORT_OPC, true).setAction(act).build().setSerializable(false);
	}

	public void makeImportGroupAddressAction() {
		Action act;
		act = new Action(Permission.READ, new AddImportGroupAddressHandler());
		act.addParameter(new Parameter(ATTR_PROJECT_CONTENT_GROUP_ADDRESS, ValueType.STRING)
				.setEditorType(EditorType.TEXT_AREA));
		act.addParameter(new Parameter(ATTR_PROJECT_NAMING_CONVENTION, ValueType.BOOL));
		node.createChild(ACTION_IMPORT_GROUP_ADDRESS, true).setAction(act).build().setSerializable(false);
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

	protected class AddImportMasterDataHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			importMasterData(event);
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

	protected class AddImportGroupAddressHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			importProjectByGroupAddress(event);
		}
	}

	void restoreLastSession() {
		if (null == node.getChildren())
			return;
	}

	public KnxConnection getConnection() {
		return this.conn;
	}

	public Node getNode() {
		return node;
	}

	public void buildDataPoint(Node parent, GroupAddressBean addressBean) {

	}

	public Node buildFolderTree(Node node2, Queue<String> queue) {
		return null;
	}

	protected abstract void edit(ActionResult event);

	protected abstract void addPoint(ActionResult event);

	protected abstract void addFolder(String name);

	protected void importMasterData(ActionResult event) {
	};

	protected void importProjectByXml(ActionResult event) {
	};

	protected void importProjectByEsf(ActionResult event) {
	};

	protected void importProjectByGroupAddress(ActionResult event) {
	};
}
