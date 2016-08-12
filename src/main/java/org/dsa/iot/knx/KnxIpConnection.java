package org.dsa.iot.knx;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.dslink.util.handler.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
import tuwien.auto.calimero.knxnetip.util.DeviceDIB;
import tuwien.auto.calimero.knxnetip.util.HPAI;
import tuwien.auto.calimero.knxnetip.util.ServiceFamiliesDIB;
import tuwien.auto.calimero.link.KNXLinkClosedException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicationBase;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListener;

public class KnxIpConnection extends KnxConnection {
	static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(KnxIpConnection.class);
	}

	static final String ACTION_ADD_DEVICE = "add device";
	static final String ACTION_DISCOVER_DEVICES = "discover devices";
	static final String ATTR_NAME = "name";
	static final String ATTR_TRANSMISSION_TYPE = "transmission type";
	static final String ATTR_GROUP_LEVEL = "group address type";
	static final String ATTR_LOCAL_HOST = "local host";
	static final String ATTR_REMOTE_HOST = "remote host";
	static final String ATTR_REMOTE_PORT = "remote port";
	static final String ATTR_USE_NAT = "use NAT";
	static final String ATTR_POLLING_INTERVAL = "polling interval";
	static final String ATTR_POLLING_TIMEOUT = "polling timeout";
	static final String NODE_STATUS = "STATUS";
	static final String STATUS_CONNECTED = "connected";

	static final int SEARCH_TIMEOUT = 5;
	static final int POLLING_INTERVAL = 5;
	static final int POLLING_TIMEOUT = 5;
	static final int DEFAULT_DELAY = 5;
	static final int INITIAL_DELAY = 0;

	boolean useNat = false;
	static ScheduledThreadPoolExecutor stpe;
	private final Map<String, ScheduledFuture<?>> pointToFutures;

	KNXNetworkLink networkLink;
	ProcessCommunicator communicator;
	InetSocketAddress localEP;
	InetSocketAddress remoteEP;
	TransmissionType transType;
	GroupAddressType groupLevel;

	String localHost;
	String remoteHost;
	int port;
	int interval;
	int timeout;

	Map<String, HPAI> addressToHPAI;
	Map<String, DeviceDIB> addressToDeviceDIB;
	Map<String, ServiceFamiliesDIB> addressToServiceFamiliesDIB;
	Set<DeviceNode> deviceSet;

	Poller poller;
	Discoverer discoverer;
	ScheduledFuture<?> discoverFuture;

	private Node statusNode;

	public KnxIpConnection(KnxLink link, Node node) {
		super(link, node);

		this.transType = TransmissionType.parseType(node.getAttribute(ATTR_TRANSMISSION_TYPE).getString());
		this.groupLevel = GroupAddressType.parseType(node.getAttribute(ATTR_GROUP_LEVEL).getString());
		this.localHost = node.getAttribute(ATTR_LOCAL_HOST).getString();
		this.localEP = (null == localHost || localHost.isEmpty()) ? null : new InetSocketAddress(localHost, 0);
		this.remoteHost = node.getAttribute(ATTR_REMOTE_HOST).getString();
		this.remoteEP = (null == remoteHost || remoteHost.isEmpty()) ? null : new InetSocketAddress(remoteHost, port);
		this.port = node.getAttribute(ATTR_REMOTE_PORT).getNumber().intValue();
		this.useNat = node.getAttribute(ATTR_USE_NAT).getBool();
		this.interval = node.getAttribute(ATTR_POLLING_INTERVAL).getNumber().intValue();
		this.timeout = node.getAttribute(ATTR_POLLING_TIMEOUT).getNumber().intValue();

		groupToPoints = new HashMap<>();
		pointToFutures = new HashMap<>();
		stpe = Objects.createDaemonThreadPool();
		addressToDeviceDIB = new HashMap<String, DeviceDIB>();
		deviceSet = new HashSet<DeviceNode>();
		try {
			networkLink = new KNXNetworkLinkIP(TransmissionType.parseServiceMode(transType), localEP, remoteEP, useNat,
					TPSettings.TP1);
			this.statusNode = node.createChild(NODE_STATUS).setValueType(ValueType.STRING)
					.setValue(new Value(STATUS_CONNECTED)).build();
			this.statusNode.setSerializable(false);
		} catch (KNXException | InterruptedException e) {
			this.statusNode.setValue(new Value(e.getMessage()));
		}
		networkLink.addLinkListener(new NetworkListener());

		try {
			communicator = new ProcessCommunicatorImpl(networkLink);
		} catch (KNXLinkClosedException e) {
			e.printStackTrace();
		}
		communicator.addProcessListener(new ProcessCommunicatorListener());
	}

	public void init() {
		makeEditAction();
		makeRemoveAction();
		makeDiscoverAction();
	}

	public KnxConnection getConnection() {
		return this;
	}

	public void makeEditAction() {
		Action act = new Action(Permission.READ, new EditHandler());
		act.addParameter(new Parameter(ATTR_NAME, ValueType.STRING, new Value(node.getName())));
		act.addParameter(
				new Parameter(ATTR_TRANSMISSION_TYPE, ValueType.makeEnum(Utils.enumNames(TransmissionType.class))));
		act.addParameter(new Parameter(ATTR_GROUP_LEVEL, ValueType.makeEnum(Utils.enumNames(GroupAddressType.class))));
		act.addParameter(new Parameter(ATTR_LOCAL_HOST, ValueType.STRING, node.getAttribute(ATTR_LOCAL_HOST)));
		act.addParameter(new Parameter(ATTR_REMOTE_HOST, ValueType.STRING, node.getAttribute(ATTR_REMOTE_HOST)));
		act.addParameter(new Parameter(ATTR_REMOTE_PORT, ValueType.NUMBER, node.getAttribute(ATTR_REMOTE_PORT)));
		act.addParameter(new Parameter(ATTR_USE_NAT, ValueType.BOOL, node.getAttribute(ATTR_USE_NAT)));
		act.addParameter(
				new Parameter(ATTR_POLLING_INTERVAL, ValueType.NUMBER, node.getAttribute(ATTR_POLLING_INTERVAL)));
		act.addParameter(
				new Parameter(ATTR_POLLING_TIMEOUT, ValueType.NUMBER, node.getAttribute(ATTR_POLLING_TIMEOUT)));

		Node actionNode = node.getChild(ACTION_EDIT);
		if ( null == actionNode)
			node.createChild(ACTION_EDIT).setAction(act).build().setSerializable(false);
		else
			actionNode.setAction(act);
	}

	public void makeDiscoverAction() {
		Action act = new Action(Permission.READ, new DeviceDiscoveryHandler());
		Node actionNode = node.getChild(ACTION_DISCOVER_DEVICES);
		if ( null == actionNode)
			node.createChild(ACTION_DISCOVER_DEVICES).setAction(act).build().setSerializable(false);
		else
			actionNode.setAction(act);
	}

	private class EditHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {

			String remoteHost = event.getParameter(ATTR_REMOTE_HOST, ValueType.STRING).getString();
			int port = event.getParameter(ATTR_REMOTE_PORT, ValueType.NUMBER).getNumber().intValue();
			String localHost = event.getParameter(ATTR_LOCAL_HOST, ValueType.STRING).getString();
			String transmission = event.getParameter(ATTR_TRANSMISSION_TYPE, ValueType.STRING).getString();
			String groupAddress = event.getParameter(ATTR_GROUP_LEVEL, ValueType.STRING).getString();
			boolean useNat = event.getParameter(ATTR_USE_NAT, ValueType.BOOL).getBool();
			int interval = event.getParameter(ATTR_POLLING_INTERVAL, ValueType.NUMBER).getNumber().intValue();

			node.setAttribute(ATTR_TRANSMISSION_TYPE, new Value(transmission));
			node.setAttribute(ATTR_GROUP_LEVEL, new Value(groupAddress));
			node.setAttribute(ATTR_REMOTE_PORT, new Value(port));
			node.setAttribute(ATTR_LOCAL_HOST, new Value(localHost));
			node.setAttribute(ATTR_REMOTE_HOST, new Value(remoteHost));
			node.setAttribute(ATTR_USE_NAT, new Value(useNat));
			node.setAttribute(ATTR_POLLING_INTERVAL, new Value(interval));
		}
	}

	private class DeviceDiscoveryHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			boolean isRestore = false;
			discover(isRestore);
		}
	}

	private class NetworkListener implements NetworkLinkListener {

		public void indication(FrameEvent e) {

		}

		public void linkClosed(CloseEvent e) {

		}

		public void confirmation(FrameEvent e) {

		}
	}

	private class ProcessCommunicatorListener implements ProcessListener {

		public void groupWrite(ProcessEvent e) {

		}

		public void detached(DetachEvent e) {

		}
	}

	private class Poller implements Runnable {
		KnxConnection listener;

		public void addListener(KnxConnection listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			try {
				poll(groupToPoints);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class Discover implements Runnable {
		KnxConnection listener;
		boolean isRestore;

		public Discover(boolean isRestore) {
			this.isRestore = isRestore;
		}

		public void addListener(KnxConnection listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			try {
				if (null == discoverer) {
					discoverer = new Discoverer(null, 0, false, true);
				}
				discoverer.startSearch(SEARCH_TIMEOUT, true);

				SearchResponse[] responses = discoverer.getSearchResponses();
				for (SearchResponse sr : responses) {
					DeviceDIB dib = sr.getDevice();
					HPAI hpai = sr.getControlEndpoint();
					ServiceFamiliesDIB fam = sr.getServiceFamilies();

					LOGGER.info("Device Discovered:");
					LOGGER.info(dib.getName() + " : " + dib.getMACAddressString());
					LOGGER.info("HPAI : " + hpai.toString());
					LOGGER.info("Family : " + fam.toString());

					addressToDeviceDIB.put(hpai.getAddress().getHostAddress(), dib);
					this.listener.onDiscovered(isRestore);
				}

				if (responses.length > 0) {
					discoverFuture.cancel(true);
				}
			} catch (KNXException e) {
				LOGGER.debug("error: ", e);
			} catch (InterruptedException e) {
				LOGGER.debug("error: ", e);
			}
		}
	}

	private void discover(boolean isRestore) {
		stpe = getDaemonThreadPool();
		Discover discover = new Discover(isRestore);
		discover.addListener(getConnection());
		discoverFuture = stpe.schedule(discover, 0, TimeUnit.SECONDS);
	}

	protected DeviceNode setupDeviceNode(String host, DeviceDIB dib) {
		String name = dib.getName();
		Node child = node.createChild(name).build();
		DeviceNode deviceNode = new DeviceNode(getConnection(), null, child, dib);
		return deviceNode;
	}

	ScheduledThreadPoolExecutor getDaemonThreadPool() {
		return stpe;
	}

	int getInterval() {
		return POLLING_INTERVAL;
	}

	@Override
	public void onDiscovered(boolean isRestore) {
		Iterator it = addressToDeviceDIB.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String host = (String) pair.getKey();
			DeviceDIB dib = (DeviceDIB) pair.getValue();
			DeviceNode device = setupDeviceNode(host, dib);
			deviceSet.add(device);
		}

		if (isRestore && !deviceSet.isEmpty()) {
			for (DeviceNode device : deviceSet) {
				device.restoreLastSession();
			}
		}
	}

	@Override
	public void stopPolling(DevicePoint point) {
		String address = point.getGroupAddress().toString();
		ScheduledFuture<?> future = pointToFutures.remove(address);
		if (!point.isSubscribed) {
			if ( null != future) {
				future.cancel(false);
				future = null;
			}
		}
	}

	@Override
	public void startPolling(DevicePoint point) {
		String address = point.getGroupAddress().toString();
		if (pointToFutures.containsKey(address)) {
			return;
		}

		LOGGER.info("Polling: ");
		stpe = getDaemonThreadPool();
		if (null == poller) {
			poller = new Poller();
			poller.addListener(getConnection());
		}

		ScheduledFuture<?> future = stpe.scheduleWithFixedDelay(poller, INITIAL_DELAY, DEFAULT_DELAY, TimeUnit.SECONDS);
		pointToFutures.put(address, future);
	}

	private void readPoint(String group, EditablePoint point) {
		PointType type = point.getType();
		GroupAddress addr = point.getGroupAddress();

		Node pointNode = point.node;
		String valString = null;

		try {
			switch (type) {
			case BOOL: {
				valString = Boolean.toString(communicator.readBool(addr));
				break;
			}
			case CONTROL: {
				valString = Integer.toString(communicator.readControl(addr));
				break;
			}
			case FLOAT2: {
				valString = Float.toString(communicator.readFloat(addr, false));
				break;
			}
			case FLOAT4: {
				valString = Float.toString(communicator.readFloat(addr, true));
				break;
			}
			case UNSIGNED: {
				// should be able to choose between UNSCALED, SCALING, and
				// ANGLE
				valString = Integer.toString(communicator.readUnsigned(addr, ProcessCommunicationBase.UNSCALED));
				break;
			}
			default: {
				break;
			}

			}

			Value v = new Value(valString);
			ValueType vt = ValueType.STRING;
			if (valString.length() == 0) {
				vt = pointNode.getValueType();
				v = null;
			}

			if (type == PointType.BOOL) {
				vt = ValueType.BOOL;
				v = new Value(Boolean.parseBoolean(valString));
			} else if (type == PointType.CONTROL) {
				vt = ValueType.BOOL;
				v = new Value(Boolean.parseBoolean(valString));
			} else if (type == PointType.FLOAT2 || type == PointType.FLOAT4) {
				vt = ValueType.BOOL;
				v = new Value(Float.parseFloat(valString));
			} else if (type == PointType.UNSIGNED) {
				vt = ValueType.NUMBER;
				v = new Value(Integer.parseInt(valString));
			} else if (type == PointType.STRING) {
				vt = ValueType.STRING;
				v = new Value(valString);
			}

			pointNode.setValueType(vt);
			pointNode.setValue(v);
			LOGGER.debug("read and updated " + pointNode.getName() + " : " + v.toString());

		} catch (KNXException | InterruptedException e) {
			LOGGER.debug("error: ", e);
		}
	}

	private void poll(Map<String, List<EditablePoint>> groupToPoints) throws InterruptedException {
		for (Entry<String, List<EditablePoint>> entry : groupToPoints.entrySet()) {
			String group = entry.getKey();
			List<EditablePoint> points = entry.getValue();
			for (EditablePoint point : points) {
				if (point.isSubscribed()) {
					readPoint(group, point);
				}
			}
		}
	}

	public void setPointValue(EditablePoint point, Value val) {

		PointType type = point.getType();
		GroupAddress dst = point.getGroupAddress();
		boolean use4ByteFloat = true;
		int stepcode = 1;
		String scale = "1";

		try {
			switch (type) {
			case BOOL: {
				communicator.write(dst, val.getBool());
				break;
			}
			case CONTROL: {
				communicator.write(dst, val.getBool(), stepcode);
				break;
			}
			case FLOAT2: {
				communicator.write(dst, val.getNumber().floatValue(), !use4ByteFloat);
				break;
			}
			case FLOAT4: {
				communicator.write(dst, val.getNumber().floatValue(), use4ByteFloat);
				break;
			}
			case UNSIGNED: {
				// should be able to choose between UNSCALED, SCALING, and
				// ANGLE
				communicator.write(dst, val.getNumber().intValue(), scale);
				break;
			}
			default: {
				break;
			}

			}
			LOGGER.debug(point.getGroupAddress() + " : " + val.getString());

		} catch (KNXException e) {
			LOGGER.debug("error: ", e);
		}
	}

	private void handleSub(final DevicePoint point, final Node event) {
		point.startPolling();
		LOGGER.debug("subscribed to " + point.node.getName());
	}

	private void handleUnsub(DevicePoint point, Node event) {
		point.stopPolling();
		LOGGER.debug("unsubscribed from " + point.node.getName());
	}

	public void setupPointListener(final DevicePoint point) {
		final Node child = point.node;

		child.getListener().setOnSubscribeHandler(new Handler<Node>() {
			public void handle(final Node event) {
				handleSub(point, event);
			}
		});

		child.getListener().setOnUnsubscribeHandler(new Handler<Node>() {
			@Override
			public void handle(Node event) {
				handleUnsub(point, event);
			}
		});
	}

	@Override
	public GroupAddressType getGroupLevel() {
		return groupLevel;
	}

	@Override
	public void updateGroupToPoints(String group, DevicePoint point) {
		List<EditablePoint> points = null;
		Map<String, List<EditablePoint>> groupToPoints = getConnection().getGroupToPoints();
		if (!groupToPoints.containsKey(group)) {
			points = new ArrayList<>();
		} else {
			points = groupToPoints.get(group);
		}
		points.add(point);
		groupToPoints.put(group, points);
	}

	public void restoreLastSession() {
		init();

		Map<String, Node> children = node.getChildren();
		if (null == children)
			return;

		for (Node child : children.values()) {
			restoreDevice(child);
		}

	}

	private void restoreDevice(Node child) {
		final Value medium = child.getAttribute(DeviceNode.ATTR_MEDIUM);
		final Value individualAddress = child.getAttribute(DeviceNode.ATTR_INDIVIDUAL_ADDRESS);
		final Value macAddress = child.getAttribute(DeviceNode.ATTR_MAC_ADDRESS);
		Value restType = child.getAttribute(EditableFolder.ATTR_RESTORE_TYPE);
		boolean isRestore = true;
		if (null != macAddress && null != individualAddress && null != medium && null != restType) {
			discover(isRestore);
		} else if (null == child.getAction() && !NODE_STATUS.equals(child.getName())) {
			node.removeChild(child);
		}
	}
}
