package org.dsa.iot.knx;

import java.net.InetSocketAddress;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

public class KnxIPTunnelingConnection extends KnxIPConnection {

	final static String ATTR_GATEWAY_NAME = "Gateway";

	public KnxIPTunnelingConnection(KnxLink link, Node node) {
		super(link, node);

		this.useNat = node.getAttribute(ATTR_USE_NAT).getBool();
		this.deviceAddress = node.getAttribute(ATTR_DEVICE_ADDRESS).getString();
		this.port = node.getAttribute(ATTR_REMOTE_PORT).getNumber().intValue();
		this.localHost = node.getAttribute(ATTR_LOCAL_HOST).getString();
		this.localEP = (null == localHost || localHost.isEmpty()) ? null : new InetSocketAddress(localHost, 0);
		this.remoteHost = node.getAttribute(ATTR_REMOTE_HOST).getString();
		this.remoteEP = (null == remoteHost || remoteHost.isEmpty()) ? null : new InetSocketAddress(remoteHost, port);
	}

	@Override
	public void init() {
		super.init();

		makeEditAction();
		connect();
	}

	@Override
	protected void connect() {
		super.connect();
		if (null != networkLink && null != communicator) {
			generateGatewayNode();
		}
	}

	private void generateGatewayNode() {
		Node child = node.createChild(ATTR_GATEWAY_NAME).build();
		child.setAttribute(ATTR_DEVICE_ADDRESS, new Value(deviceAddress));
		new DeviceNode(getConnection(), null, child);
	}

	@Override
	public void makeEditAction() {
		Action act = new Action(Permission.READ, new EditHandler());
		act.addParameter(new Parameter(ATTR_NAME, ValueType.STRING, new Value(node.getName())));
		act.addParameter(new Parameter(ATTR_TRANSMISSION_TYPE,
				ValueType.makeEnum(TransmissionType.Tunneling.toString()), node.getAttribute(ATTR_TRANSMISSION_TYPE)));
		act.addParameter(new Parameter(ATTR_GROUP_LEVEL, ValueType.makeEnum(Utils.enumNames(GroupAddressType.class)),
				node.getAttribute(ATTR_GROUP_LEVEL)));
		act.addParameter(new Parameter(ATTR_LOCAL_HOST, ValueType.STRING, node.getAttribute(ATTR_LOCAL_HOST)));
		act.addParameter(new Parameter(ATTR_REMOTE_HOST, ValueType.STRING, node.getAttribute(ATTR_REMOTE_HOST)));
		act.addParameter(new Parameter(ATTR_REMOTE_PORT, ValueType.NUMBER, node.getAttribute(ATTR_REMOTE_PORT)));
		act.addParameter(new Parameter(ATTR_USE_NAT, ValueType.BOOL, node.getAttribute(ATTR_USE_NAT)));
		act.addParameter(new Parameter(ATTR_DEVICE_ADDRESS, ValueType.STRING, node.getAttribute(ATTR_DEVICE_ADDRESS)));
		act.addParameter(
				new Parameter(ATTR_POLLING_INTERVAL, ValueType.NUMBER, node.getAttribute(ATTR_POLLING_INTERVAL)));
		act.addParameter(
				new Parameter(ATTR_POLLING_TIMEOUT, ValueType.NUMBER, node.getAttribute(ATTR_POLLING_TIMEOUT)));

		Node actionNode = node.getChild(ACTION_EDIT);
		if (null == actionNode)
			node.createChild(ACTION_EDIT).setAction(act).build().setSerializable(false);
		else
			actionNode.setAction(act);
	}

	private class EditHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			transType = TransmissionType
					.parseType(event.getParameter(ATTR_TRANSMISSION_TYPE, ValueType.STRING).getString());
			groupLevel = GroupAddressType.parseType(event.getParameter(ATTR_GROUP_LEVEL, ValueType.STRING).getString());
			localHost = event.getParameter(ATTR_LOCAL_HOST, ValueType.STRING).getString();
			remoteHost = event.getParameter(ATTR_REMOTE_HOST, ValueType.STRING).getString();
			port = event.getParameter(ATTR_REMOTE_PORT, ValueType.NUMBER).getNumber().intValue();
			useNat = event.getParameter(ATTR_USE_NAT, ValueType.BOOL).getBool();
			deviceAddress = event.getParameter(ATTR_DEVICE_ADDRESS, ValueType.STRING).getString();
			interval = event.getParameter(ATTR_POLLING_INTERVAL, ValueType.NUMBER).getNumber().intValue();

			node.setAttribute(ATTR_TRANSMISSION_TYPE, new Value(transType.toString()));
			node.setAttribute(ATTR_GROUP_LEVEL, new Value(groupLevel.toString()));
			node.setAttribute(ATTR_LOCAL_HOST, new Value(localHost));
			node.setAttribute(ATTR_REMOTE_HOST, new Value(remoteHost));
			node.setAttribute(ATTR_REMOTE_PORT, new Value(port));
			node.setAttribute(ATTR_USE_NAT, new Value(useNat));
			node.setAttribute(ATTR_DEVICE_ADDRESS, new Value(deviceAddress));
			node.setAttribute(ATTR_POLLING_INTERVAL, new Value(interval));

			makeEditAction();
			connect();
		}
	}

	@Override
	void createLink() {
		if (null == remoteEP) {
			statusNode.setValue(new Value(STATUS_TUNNELING_WARNNING));
		} else {
			try {
				networkLink = new KNXNetworkLinkIP((short) KNXNetworkLinkIP.TUNNELING, localEP, remoteEP, useNat,
						new TPSettings(new IndividualAddress(deviceAddress)));
			} catch (KNXException | InterruptedException e) {
				statusNode.setValue(new Value(e.getMessage()));
			}
		}
	}
}
