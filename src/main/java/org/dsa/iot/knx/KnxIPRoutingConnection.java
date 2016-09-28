package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;

import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

public class KnxIPRoutingConnection extends KnxIPConnection {

	public KnxIPRoutingConnection(KnxLink link, Node node) {
		super(link, node);
	}

	@Override
	public void init() {
		super.init();

		makeEditAction();
		makeDiscoverAction();
		connect();
	}

	@Override
	public void makeEditAction() {
		Action act = new Action(Permission.READ, new EditHandler());
		act.addParameter(new Parameter(ATTR_NAME, ValueType.STRING, new Value(node.getName())));
		act.addParameter(
				new Parameter(ATTR_TRANSMISSION_TYPE, ValueType.makeEnum(TransmissionType.Routing.toString()),
						node.getAttribute(ATTR_TRANSMISSION_TYPE)));
		act.addParameter(new Parameter(ATTR_GROUP_LEVEL, ValueType.makeEnum(Utils.enumNames(GroupAddressType.class)),
				node.getAttribute(ATTR_GROUP_LEVEL)));
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

	public void makeDiscoverAction() {
		Action act = new Action(Permission.READ, new DeviceDiscoveryHandler());
		Node actionNode = node.getChild(ACTION_DISCOVER_DEVICES);
		if (null == actionNode)
			node.createChild(ACTION_DISCOVER_DEVICES).setAction(act).build().setSerializable(false);
		else
			actionNode.setAction(act);
	}

	private class EditHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			transType = TransmissionType
					.parseType(event.getParameter(ATTR_TRANSMISSION_TYPE, ValueType.STRING).getString());
			groupLevel = GroupAddressType.parseType(event.getParameter(ATTR_GROUP_LEVEL, ValueType.STRING).getString());
			interval = event.getParameter(ATTR_POLLING_INTERVAL, ValueType.NUMBER).getNumber().intValue();
			timeout = event.getParameter(ATTR_POLLING_TIMEOUT, ValueType.NUMBER).getNumber().intValue();

			node.setAttribute(ATTR_TRANSMISSION_TYPE, new Value(transType.toString()));
			node.setAttribute(ATTR_GROUP_LEVEL, new Value(groupLevel.toString()));
			node.setAttribute(ATTR_POLLING_INTERVAL, new Value(interval));
			node.setAttribute(ATTR_POLLING_TIMEOUT, new Value(timeout));

			makeEditAction();
			connect();
		}
	}

	@Override
	KNXNetworkLink createLink() {
		KNXNetworkLink networkLink = null;
		try {
			networkLink = new KNXNetworkLinkIP(KNXNetworkLinkIP.ROUTING, null, null, false, TPSettings.TP1);
		} catch (KNXException | InterruptedException e) {
			LOGGER.debug(e.getMessage());
			statusNode.setValue(new Value(e.getMessage()));
		} 
		
		return networkLink;
	}

}
