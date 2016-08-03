package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.serializer.Deserializer;
import org.dsa.iot.dslink.serializer.Serializer;
import org.dsa.iot.dslink.util.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnxLink {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(KnxLink.class);
	}

	static final String ACTION_ADD_IP_CONNECTION = "add ip connection";
	static final String ATTR_NAME = "name";
	static final String ATTR_TRANSMISSION_TYPE = "transmission type";
	static final String ATTR_GROUP_LEVEL = "group address type";
	static final String ATTR_LOCAL_HOST = "local host";
	static final String ATTR_REMOTE_HOST = "remote host";
	static final String ATTR_REMOTE_PORT = "remote port";
	static final String ATTR_USE_NAT = "use NAT";
	static final String ATTR_POLLING_INTERVAL = "polling interval";
	static final int DEFAULT_KNX_PORT = 3671;
	static final int DEFAULT_POLLING_INTERVAL = 5;
	static final String DEFAULT_HOST_ADDRESS = "0.0.0.0";

	Node node;
	Serializer copySerializer;
	Deserializer copyDeserializer;

	private KnxLink(Node node, Serializer ser, Deserializer deser) {
		this.node = node;
		this.copySerializer = ser;
		this.copyDeserializer = deser;
	}

	public static void start(Node parent, Serializer copyser, Deserializer copydeser) {
		Node node = parent;
		final KnxLink link = new KnxLink(node, copyser, copydeser);
		link.init();
	}

	public void init() {
		restoreLastSession();
		
		makeAddIpConnection();
		makeUSBConnection();
		makeAddUartConnection();
	}

	private void makeAddIpConnection() {
		Action act = new Action(Permission.READ, new AddIpConnectionHandler());
		act.addParameter(new Parameter(ATTR_NAME, ValueType.STRING));
		act.addParameter(new Parameter(ATTR_TRANSMISSION_TYPE, ValueType.makeEnum(Utils.enumNames(TransmissionType.class))));
		act.addParameter(
				new Parameter(ATTR_GROUP_LEVEL, ValueType.makeEnum(Utils.enumNames(GroupAddressType.class))));
		act.addParameter(new Parameter(ATTR_LOCAL_HOST, ValueType.STRING, new Value(DEFAULT_HOST_ADDRESS)));
		act.addParameter(new Parameter(ATTR_REMOTE_HOST, ValueType.STRING, new Value("")));
		act.addParameter(new Parameter(ATTR_REMOTE_PORT, ValueType.NUMBER, new Value(DEFAULT_KNX_PORT)));
		act.addParameter(new Parameter(ATTR_USE_NAT, ValueType.BOOL, new Value(false)));
		act.addParameter(new Parameter(ATTR_POLLING_INTERVAL, ValueType.NUMBER, new Value(DEFAULT_POLLING_INTERVAL)));
		node.createChild(ACTION_ADD_IP_CONNECTION).setAction(act).build().setSerializable(false);
	}

	private void makeUSBConnection() {

	}

	private void makeAddUartConnection() {

	}

	public void restoreLastSession() {
 
	}

	private class AddIpConnectionHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			String name = event.getParameter(ATTR_NAME, ValueType.STRING).getString();
			String transmission = event.getParameter(ATTR_TRANSMISSION_TYPE).getString();
			String groupAddress = event.getParameter(ATTR_GROUP_LEVEL).getString();
			String localHost = event.getParameter(ATTR_LOCAL_HOST, ValueType.STRING).getString();
			String remoteHost = event.getParameter(ATTR_REMOTE_HOST, ValueType.STRING).getString();
			int port = event.getParameter(ATTR_REMOTE_PORT, ValueType.NUMBER).getNumber().intValue();
			boolean useNat = event.getParameter(ATTR_USE_NAT, ValueType.BOOL).getBool();
            int interval = event.getParameter(ATTR_POLLING_INTERVAL, ValueType.NUMBER).getNumber().intValue();
            		
			Node ipConnNode = node.createChild(name).build();
			ipConnNode.setAttribute(ATTR_TRANSMISSION_TYPE, new Value(transmission));
			ipConnNode.setAttribute(ATTR_GROUP_LEVEL, new Value(groupAddress));			
			ipConnNode.setAttribute(ATTR_LOCAL_HOST, new Value(localHost));
			ipConnNode.setAttribute(ATTR_REMOTE_HOST, new Value(remoteHost));
			ipConnNode.setAttribute(ATTR_REMOTE_PORT, new Value(port));
			ipConnNode.setAttribute(ATTR_USE_NAT, new Value(useNat));
			ipConnNode.setAttribute(ATTR_POLLING_INTERVAL, new Value(interval));
			KnxConnection conn = new KnxIpConnection(getLink(), ipConnNode);

			conn.init();
		}
	}

	private KnxLink getLink() {
		return this;
	}

}
