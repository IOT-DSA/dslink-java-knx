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
import org.dsa.iot.dslink.node.actions.ActionResult;
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
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlatorTime;
import tuwien.auto.calimero.dptxlator.DPTXlatorDate;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
import tuwien.auto.calimero.knxnetip.util.DeviceDIB;
import tuwien.auto.calimero.knxnetip.util.HPAI;
import tuwien.auto.calimero.knxnetip.util.ServiceFamiliesDIB;
import tuwien.auto.calimero.link.KNXLinkClosedException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.process.ProcessCommunicationBase;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListener;

public abstract class KnxIPConnection extends KnxConnection {
	static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(KnxIPConnection.class);
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
	static final String ATTR_DEVICE_ADDRESS = "device address";
	static final String ATTR_POLLING_INTERVAL = "polling interval";
	static final String ATTR_POLLING_TIMEOUT = "polling timeout";
	static final String NODE_STATUS = "STATUS";
	static final String STATUS_CONNECTING = "connecting";
	static final String STATUS_CONNECTED = "connected";
	static final String STATUS_DISCONNECTED = "disconnected";
	static final String STATUS_RESTORING = "restoring the last session";
	static final String STATUS_TUNNELING_WARNNING = "invalid remote host for tunneling";
	static final String MESSAGE_DISCOVERING = "Discovering devices......";
	static final String MESSAGE_DISCOVERED = "Devices discovered!";
	static final String MESSAGE_HOST_PROTOCOL_ADDRESS_INFORMATION = "Host Protocol Address Information";
	static final String MESSAGE_SERVICE_FAMILIES = "Service Families";
	static final String MESSAGE_MAP_SEPARATOR = ":";
	static final int SEARCH_TIMEOUT_IN_SECONDS = 5;
	static final int POLLING_INTERVAL = 5000;
	static final int POLLING_TIMEOUT = 5000;
	static final int INITIAL_DELAY = 0;
	static final int MAXIMUM_UNSIGNED_BYTE = 255;
	static final int PERCENTAGE_FACTOR = 100;

	private static final int GROUP_READ = 0x00;
	private static final int GROUP_RESPONSE = 0x40;
	private static final int GROUP_WRITE = 0x80;

	private ScheduledThreadPoolExecutor stpe;
	private final Map<String, ScheduledFuture<?>> pointToFutures;

	KNXNetworkLink networkLink;
	ProcessCommunicator communicator;
	InetSocketAddress localEP;
	InetSocketAddress remoteEP;
	TransmissionType transType;
	int serviceMode;
	GroupAddressType groupLevel;
	boolean useNat = false;
	String deviceAddress;
	String localHost;
	String remoteHost;
	int port;
	long interval;
	long timeout;

	Map<String, DeviceDIB> hostToDeviceDIB;
	Set<DeviceNode> deviceSet;
	Map<String, EditablePoint> addressToDataPoint;
	Map<String, Boolean> addressToPolled;

	Poller poller;
	Discoverer discoverer;
	ScheduledFuture<?> discoverFuture;

	Node statusNode;
	private boolean isRestoring;

	public KnxIPConnection(KnxLink link, Node node) {
		super(link, node);

		transType = TransmissionType.parseType(node.getAttribute(ATTR_TRANSMISSION_TYPE).getString());
		serviceMode = TransmissionType.parseServiceMode(transType);
		groupLevel = GroupAddressType.parseType(node.getAttribute(ATTR_GROUP_LEVEL).getString());
		interval = node.getAttribute(ATTR_POLLING_INTERVAL).getNumber().intValue();
		timeout = node.getAttribute(ATTR_POLLING_TIMEOUT).getNumber().intValue();

		groupToPoints = new HashMap<>();
		pointToFutures = new HashMap<>();
		stpe = Objects.createDaemonThreadPool();
		hostToDeviceDIB = new HashMap<String, DeviceDIB>();
		addressToDataPoint = new HashMap<String, EditablePoint>();
		addressToPolled = new HashMap<>();
		deviceSet = new HashSet<DeviceNode>();
	}

	public void init() {
		makeRemoveAction();
	}

	protected void disconnect() {
		if (null != communicator) {
			communicator.detach();
			communicator = null;
			if (null != networkLink) {
				networkLink.close();
				networkLink = null;
			}
		}
	}

	protected void connect() {
		statusNode = node.getChild(NODE_STATUS);
		if (null == statusNode) {
			statusNode = node.createChild(NODE_STATUS).setValueType(ValueType.STRING).build();
			statusNode.setSerializable(false);
		} else {
			statusNode.setValue(new Value(STATUS_CONNECTING));
		}

		networkLink = null;
		communicator = null;
		networkLink = createLink();
		if (null != networkLink) {
			networkLink.addLinkListener(new NetworkListener());
			try {
				communicator = new ProcessCommunicatorImpl(networkLink);
				communicator.addProcessListener(new ProcessCommunicatorListener());
			} catch (KNXLinkClosedException e) {
				this.statusNode.setValue(new Value(e.getMessage()));
			}
		}

		if (null != networkLink && null != communicator) {
			statusNode.setValue(new Value(STATUS_CONNECTED));
		}
	}

	public KnxConnection getConnection() {
		return this;
	}

	@Override
	public void remove() {
		disconnect();
		super.remove();
	}

	@Override
	public void stop() {
		disconnect();
		if (null != statusNode) {
			statusNode.setValue(new Value(STATUS_DISCONNECTED));
		}

	}

	@Override
	public void restart() {
		stop();
		this.restoreLastSession();
	}

	class DeviceDiscoveryHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			discover();
		}
	}

	private static class NetworkListener implements NetworkLinkListener {

		public void indication(FrameEvent e) {

		}

		public void linkClosed(CloseEvent e) {

		}

		public void confirmation(FrameEvent e) {

		}
	}

	private class ProcessCommunicatorListener implements ProcessListener {

		public void groupWrite(ProcessEvent e) {
			GroupAddress dstAddress = e.getDestination();
			IndividualAddress srcAddress = e.getSourceAddr();
			byte[] asdu = e.getASDU();
			int service = e.getServiceCode();
			switch (service) {
			case GROUP_READ: {
				break;
			}
			case GROUP_RESPONSE: {
				break;
			}
			case GROUP_WRITE: {
				updatePointValue(dstAddress, asdu);
				break;
			}
			default:
				break;
			}

		}

		public void detached(DetachEvent e) {

		}
	}

	private class Poller implements Runnable {

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

		public void addListener(KnxConnection listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			try {
				if (null == discoverer) {
					discoverer = new Discoverer(null, 0, false, true);
				}
				LOGGER.info(MESSAGE_DISCOVERING);
				discoverer.startSearch(SEARCH_TIMEOUT_IN_SECONDS, true);

				SearchResponse[] responses = discoverer.getSearchResponses();
				for (SearchResponse sr : responses) {
					DeviceDIB dib = sr.getDevice();
					HPAI hpai = sr.getControlEndpoint();
					ServiceFamiliesDIB fam = sr.getServiceFamilies();

					LOGGER.info(MESSAGE_DISCOVERED);
					LOGGER.info(dib.getName() + MESSAGE_MAP_SEPARATOR + dib.getMACAddressString());
					LOGGER.info(MESSAGE_HOST_PROTOCOL_ADDRESS_INFORMATION + MESSAGE_MAP_SEPARATOR + hpai.toString());
					LOGGER.info(MESSAGE_SERVICE_FAMILIES + MESSAGE_MAP_SEPARATOR + fam.toString());

					hostToDeviceDIB.put(hpai.getAddress().getHostAddress(), dib);
					this.listener.onDiscovered();
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

	private void discover() {
		stpe = getDaemonThreadPool();
		Discover discover = new Discover();
		discover.addListener(getConnection());
		discoverFuture = stpe.schedule(discover, 0, TimeUnit.SECONDS);
	}

	void updatePointValue(GroupAddress address, byte[] asdu) {
		EditablePoint point = addressToDataPoint.get(address.toString());
		if (null != point) {
			Value value = null;
			PointType type = point.getType();
			if (type == PointType.BOOL) {
				value = (asdu[0] & 0xFF) == 1 ? new Value(true) : new Value(false);
			} else if (type == PointType.CONTROL) {
				int asInt = (asdu[0] & 0xFF);
				value = new Value(asInt);
			} else if (type == PointType.UNSIGNED) {
				int asInt = asdu[0] & 0xFF;
				value = new Value(rawToPercentage(asInt));
			} else if (type == PointType.FLOAT2) {
				int asInt = (asdu[0] & 0xFF) | ((asdu[1] & 0xFF) << 8);
				float asFloat = Float.intBitsToFloat(asInt);
				value = new Value(asFloat);
			} else if (type == PointType.FLOAT4) {
				int asInt = (asdu[0] & 0xFF) | ((asdu[1] & 0xFF) << 8) | ((asdu[2] & 0xFF) << 16)
						| ((asdu[3] & 0xFF) << 24);
				float asFloat = Float.intBitsToFloat(asInt);
				value = new Value(asFloat);
			} else if (type == PointType.STRING) {
				String asString = asdu.toString();
				value = new Value(asString);
			}

			point.node.setValue(value);
		}

	}

	int rawToPercentage(int number) {
		double percentage = (double) number / (double) MAXIMUM_UNSIGNED_BYTE;
		percentage *= (double) PERCENTAGE_FACTOR;
		long rounded = Math.round(percentage);
		return (int) rounded;
	}

	int percentageToRaw(int percentage) {
		double converted = (double) percentage / (double) PERCENTAGE_FACTOR;
		converted *= (double) MAXIMUM_UNSIGNED_BYTE;
		long rounded = Math.round(converted);
		return (int) rounded;
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
	public void onDiscovered() {
		Iterator<Entry<String, DeviceDIB>> it = hostToDeviceDIB.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, DeviceDIB> pair = (Map.Entry<String, DeviceDIB>) it.next();
			String host = (String) pair.getKey();
			DeviceDIB dib = (DeviceDIB) pair.getValue();
			DeviceNode device = setupDeviceNode(host, dib);
			deviceSet.add(device);
		}

		if (isRestoring && !deviceSet.isEmpty()) {
			for (DeviceNode device : deviceSet) {
				device.restoreLastSession();
			}
			isRestoring = false;
			statusNode.setValue(new Value(STATUS_CONNECTED));
		}
	}

	@Override
	public void stopPolling(DevicePoint point) {
		String address = point.getGroupAddress().toString();
		ScheduledFuture<?> future = pointToFutures.remove(address);
		if (!point.isSubscribed) {
			if (null != future) {
				future.cancel(false);
				future = null;
			}
		}
	}

	@Override
	public void startPolling(DevicePoint point) {
		String address = point.getGroupAddress().toString();
		if (pointToFutures.containsKey(address)
				|| (addressToPolled.containsKey(address) && addressToPolled.get(address))) {
			return;
		}

		stpe = getDaemonThreadPool();
		if (null == poller) {
			poller = new Poller();
		}

		ScheduledFuture<?> future = stpe.schedule(poller, INITIAL_DELAY, TimeUnit.MILLISECONDS);
		pointToFutures.put(address, future);
	}

	private void readPoint(String group, EditablePoint point) {
		PointType type = point.getType();
		GroupAddress groupAddress = point.getGroupAddress();
		String address = groupAddress.toString();
		Node pointNode = point.node;
		String valString = null;

		try {
			switch (type) {
			case BOOL: {
				valString = Boolean.toString(communicator.readBool(groupAddress));
				break;
			}
			case CONTROL: {
				// read a 3 bit controlled datapoint value
				valString = Integer.toString(communicator.readControl(groupAddress));
				break;
			}
			case UNSIGNED: {
				// read an unsiged 8 bit datapoint value, should be able to
				// choose between UNSCALED, SCALING, and
				// ANGLE
				valString = Integer
						.toString(communicator.readUnsigned(groupAddress, ProcessCommunicationBase.UNSCALED));
				break;
			}
			case FLOAT2: {
				valString = Float.toString(communicator.readFloat(groupAddress, false));
				break;
			}
			case FLOAT4: {
				valString = Float.toString(communicator.readFloat(groupAddress, true));
				break;
			}
			case TIME: {
				Datapoint dataPnt = new StateDP(groupAddress, "time", 0, DPTXlatorTime.DPT_TIMEOFDAY.getID());
				String response = communicator.read(dataPnt);
				valString = response;
				break;
			}
			case DATE: {
				Datapoint dataPnt = new StateDP(groupAddress, "date", 0, DPTXlatorDate.DPT_DATE.getID());
				String response = communicator.read(dataPnt);
				valString = response;
				break;
			}
			case STRING: {
				valString = communicator.readString(groupAddress);
				break;
			}
			default: {
				break;
			}

			}

			Value value = new Value(valString);
			ValueType valueType = ValueType.STRING;
			if (null != valString && valString.length() == 0) {
				valueType = pointNode.getValueType();
				value = null;
			}

			if (type == PointType.BOOL) {
				valueType = ValueType.BOOL;
				value = new Value(Boolean.parseBoolean(valString));
			} else if (type == PointType.CONTROL) {
				valueType = ValueType.NUMBER;
				value = new Value(Integer.parseInt(valString));
			} else if (type == PointType.UNSIGNED) {
				valueType = ValueType.NUMBER;
				int rawValue = Integer.parseInt(valString);
				value = new Value(rawToPercentage(rawValue));
			} else if (type == PointType.FLOAT2 || type == PointType.FLOAT4) {
				valueType = ValueType.NUMBER;
				value = new Value(Float.parseFloat(valString));
			} else if (type == PointType.TIME) {
				valueType = ValueType.STRING;
				value = new Value(valString);
			} else if (type == PointType.DATE) {
				valueType = ValueType.STRING;
				value = new Value(valString);
			} else if (type == PointType.STRING) {
				valueType = ValueType.STRING;
				value = new Value(valString);
			}

			pointNode.setValueType(valueType);
			pointNode.setValue(value);
			addressToPolled.put(address, true);
			LOGGER.debug("read and updated " + pointNode.getName() + " : " + value.toString());

		} catch (KNXException | InterruptedException e) {
			LOGGER.debug("error: ", e);
		}
	}

	private void poll(Map<String, List<EditablePoint>> groupToPoints) throws InterruptedException {
		for (Entry<String, List<EditablePoint>> entry : groupToPoints.entrySet()) {
			String group = entry.getKey();
			List<EditablePoint> points = entry.getValue();
			for (EditablePoint point : points) {
				Node node = point.node;
				String address = node.getAttribute(EditablePoint.ATTR_GROUP_ADDRESS).getString();
				boolean polled = addressToPolled.get(address);
				if (point.isSubscribed() && !polled) {
					LOGGER.info("Polling: ");
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
			case UNSIGNED: {
				// should be able to choose between UNSCALED, SCALING, and
				// ANGLE
				int value = val.getNumber().intValue();
				communicator.write(dst, percentageToRaw(value), ProcessCommunicationBase.UNSCALED);
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
			case TIME: {
				Datapoint dataPnt = new StateDP(dst, "time", 0, DPTXlatorTime.DPT_TIMEOFDAY.getID());
				communicator.write(dataPnt, val.getString());
				break;
			}
			case DATE: {
				Datapoint dataPnt = new StateDP(dst, "date", 0, DPTXlatorDate.DPT_DATE.getID());
				communicator.write(dataPnt, val.getString());
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
	public void updateGroupToPoints(String group, DevicePoint point, boolean add) {
		List<EditablePoint> points = null;
		Map<String, List<EditablePoint>> groupToPoints = getConnection().getGroupToPoints();
		if (!groupToPoints.containsKey(group)) {
			points = new ArrayList<>();
		} else {
			points = groupToPoints.get(group);
		}

		if (add) {
			points.add(point);
		} else {
			points.remove(point);
		}
		groupToPoints.put(group, points);
	}

	@Override
	public void updateAddressToPoint(String address, EditablePoint point, boolean add) {
		if (add) {
			addressToDataPoint.put(address, point);
			addressToPolled.put(address, false);
		} else {
			addressToDataPoint.remove(address);
			addressToPolled.remove(address);
		}
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

		if (null != macAddress && null != individualAddress && null != medium && null != restType) {
			isRestoring = true;
			discover();
		} else if (null == child.getAction() && !NODE_STATUS.equals(child.getName())) {
			node.removeChild(child);
		}
	}

	abstract KNXNetworkLink createLink();

	abstract void makeEditAction();
}
