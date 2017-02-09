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
import org.dsa.iot.knx.datapoint.DPT;
import org.dsa.iot.knx.datapoint.DPT1BitControlled;
import org.dsa.iot.knx.datapoint.DPT2ByteFloat;
import org.dsa.iot.knx.datapoint.DPT2ByteUnsigned;
import org.dsa.iot.knx.datapoint.DPT3BitControlled;
import org.dsa.iot.knx.datapoint.DPT4ByteFloat;
import org.dsa.iot.knx.datapoint.DPT64BitSigned;
import org.dsa.iot.knx.datapoint.DPT8BitUnsigned;
import org.dsa.iot.knx.datapoint.DPTBoolean;
import org.dsa.iot.knx.datapoint.DPTDate;
import org.dsa.iot.knx.datapoint.DPTDateTime;
import org.dsa.iot.knx.datapoint.DPTRGB;
import org.dsa.iot.knx.datapoint.DPTSceneControl;
import org.dsa.iot.knx.datapoint.DPTSceneNumber;
import org.dsa.iot.knx.datapoint.DPTString;
import org.dsa.iot.knx.datapoint.DPTTime;
import org.dsa.iot.knx.datapoint.DatapointType;
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
import tuwien.auto.calimero.dptxlator.DPTXlator1BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlator3BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlatorTime;
import tuwien.auto.calimero.dptxlator.DPTXlatorDate;
import tuwien.auto.calimero.dptxlator.DPTXlatorRGB;
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
	static final String ATTR_INDIVIDUAL_ADDRESS = "individual address";
	static final String ATTR_MEDIUM = "medium";
	static final String ATTR_POLLING_INTERVAL = "polling interval";
	static final String ATTR_POLLING_TIMEOUT = "polling timeout";
	static final String ATTR_UNIT = "unit";
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
	String individualAddress;
	String localHost;
	String remoteHost;
	int port;
	long interval;
	long timeout;

	Map<String, DeviceDIB> hostToDeviceDIB;
	Map<String, DeviceNode> nameToDeviceNode;
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
		nameToDeviceNode = new HashMap<String, DeviceNode>();
	}

	public void init() {
		makeRemoveAction();
	}

	protected void disconnect() {
		if (null != communicator) {
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
			DatapointType type = point.getType();
			String unit = null;
			DPT dpt = type.getDpt();
			unit = dpt.getUnit();
			if (dpt instanceof DPTBoolean) {
				value = new Value((asdu[0] & 0x01) == 1);
			} else if (dpt instanceof DPT1BitControlled) {
				int asInt = (asdu[0] & 0x03);
				value = new Value(asInt);
			} else if (dpt instanceof DPT3BitControlled) {
				int asInt = (asdu[0] & 0x0F);
				value = new Value(asInt);
			} else if (dpt instanceof DPT8BitUnsigned) {
				if (DatapointType.EIGHT_BIT_UNSIGNED_SCALING.getTypeId().equals(type.getTypeId())
						|| DatapointType.EIGHT_BIT_UNSIGNED_ANGLE.getTypeId().equals(type.getTypeId())) {
					int asInt = asdu[0] & 0x000000FF;
					value = new Value(asInt);
				} else {
					int asInt = asdu[0] & 0x000000FF;
					value = new Value(rawToPercentage(asInt));
				}
			} else if (dpt instanceof DPTSceneNumber || dpt instanceof DPTSceneControl) {
				int asInt = asdu[0] & 0x0000003F;
				value = new Value(asInt);

			} else if (dpt instanceof DPT2ByteUnsigned) {
				int asInt = (asdu[0] & 0x000000FF) | ((asdu[1] & 0x000000FF) << 8);
				value = new Value(asInt);
			} else if (dpt instanceof DPT2ByteFloat) {
				float asFloat = 0;
				DPTXlator translator;
				try {
					translator = new DPTXlator2ByteFloat(dpt.getDtpId());
					if (asdu.length < 2) {
						throw new KNXIllegalArgumentException("minimum APDU length is 2 bytes");
					}
					translator.setData(asdu, 0);
					asFloat = (float) translator.getNumericValue();
				} catch (KNXFormatException e) {
					LOGGER.error(e.getMessage());
				}

				value = new Value(asFloat);
			} else if (dpt instanceof DPT4ByteFloat) {
				float asFloat = 0;
				DPTXlator translator;
				try {
					translator = new DPTXlator4ByteFloat(dpt.getDtpId());
					if (asdu.length < 4) {
						throw new KNXIllegalArgumentException("minimum APDU length is 4 bytes");
					}
					translator.setData(asdu, 0);
					asFloat = (float) translator.getNumericValue();
				} catch (KNXFormatException e) {
					LOGGER.error(e.getMessage());
				}

				value = new Value(asFloat);
			} else if (dpt instanceof DPTDate) {
				String date = null;
				DPTXlator translator;
				try {
					translator = new DPTXlatorDate(dpt.getDtpId());
					if (asdu.length < 3) {
						throw new KNXIllegalArgumentException("minimum APDU length is 3 bytes");
					}
					translator.setData(asdu, 0);
					date = translator.getValue();
				} catch (KNXFormatException e) {
					LOGGER.error(e.getMessage());
				}

				value = new Value(date);
			} else if (dpt instanceof DPTTime) {
				String time = null;
				DPTXlator translator;
				try {
					translator = new DPTXlatorTime(dpt.getDtpId());
					if (asdu.length < 3) {
						throw new KNXIllegalArgumentException("minimum APDU length is 3 bytes");
					}
					translator.setData(asdu, 0);
					time = translator.getValue();
				} catch (KNXFormatException e) {
					LOGGER.error(e.getMessage());
				}

				value = new Value(time);
			} else if (dpt instanceof DPTRGB) {
				String rgb = null;
				DPTXlator translator;
				try {
					translator = new DPTXlatorRGB(dpt.getDtpId());
					if (asdu.length < 3) {
						throw new KNXIllegalArgumentException("minimum APDU length is 3 bytes");
					}
					translator.setData(asdu, 0);
					rgb = translator.getValue();
				} catch (KNXFormatException e) {
					LOGGER.error(e.getMessage());
				}

				value = new Value(rgb);
			} else if (dpt instanceof DPTDateTime) {
				String datetime = null;
				DPTXlator translator;
				try {
					translator = new DPTXlatorTime(dpt.getDtpId());
					if (asdu.length < 8) {
						throw new KNXIllegalArgumentException("minimum APDU length is 8 bytes");
					}
					translator.setData(asdu, 0);
					datetime = translator.getValue();
				} catch (KNXFormatException e) {
					LOGGER.error(e.getMessage());
				}

				value = new Value(datetime);
			} else if (dpt instanceof DPT64BitSigned) {
				String kiloString = null;
				DPTXlator translator;
				try {
					translator = new DPTXlatorTime(dpt.getDtpId());
					if (asdu.length < 8) {
						throw new KNXIllegalArgumentException("minimum APDU length is 8 bytes");
					}
					translator.setData(asdu, 0);
					kiloString = translator.getValue();
				} catch (KNXFormatException e) {
					LOGGER.error(e.getMessage());
				}
				long kiloValue = Long.parseLong(kiloString);
				value = new Value(kiloValue);
			} else if (dpt instanceof DPTString) {
				String asString = asdu.toString();
				value = new Value(asString);
			} else {
				String asString = asdu.toString();
				value = new Value(asString);
			}

			point.node.setValue(value);
			if (unit != null) {
				point.node.setAttribute(ATTR_UNIT, new Value(unit));
			}
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
		DeviceNode deviceNode = null;
		String name = dib.getName();
		if (!nameToDeviceNode.containsKey(name)) {
			Node child = node.getChild(name);
			if (null == child) {
				child = node.createChild(name).build();
			}
			deviceNode = new DeviceNode(getConnection(), null, child, dib);
		} else {
			deviceNode = nameToDeviceNode.get(name);
		}

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
			if (null != device) {
				nameToDeviceNode.put(dib.getName(), device);
			}

		}

		if (isRestoring && !nameToDeviceNode.isEmpty()) {
			for (DeviceNode device : nameToDeviceNode.values()) {
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

	public void readPoint(String group, EditablePoint point) {
		DatapointType type = point.getType();
		GroupAddress groupAddress = point.getGroupAddress();
		String address = groupAddress.toString();
		Node pointNode = point.node;
		Value value = null;
		ValueType valueType = null;
		Integer valInt = null;
		Float valFloat = null;
		Boolean valBoolean = null;
		String valString = null;
		String unit = null;

		DPT dpt = type.getDpt();
		unit = dpt.getUnit();
		try {
			if (dpt instanceof DPTBoolean) {
				valBoolean = communicator.readBool(groupAddress);
				value = new Value(valBoolean);
				valueType = ValueType.BOOL;
			} else if (dpt instanceof DPT1BitControlled) {
				Datapoint dataPnt = new StateDP(groupAddress, "1 bit controlled", 0, dpt.getDtpId());
				valString = communicator.read(dataPnt);
				value = new Value(valString);
				valueType = ValueType.STRING;
			} else if (dpt instanceof DPT3BitControlled) {
				valInt = communicator.readControl(groupAddress);
				value = new Value(valInt);
				valueType = ValueType.NUMBER;
			} else if (dpt instanceof DPT8BitUnsigned) {
				if (DatapointType.EIGHT_BIT_UNSIGNED_SCALING.getTypeId().equals(type.getTypeId())) {
					valInt = communicator.readUnsigned(groupAddress, ProcessCommunicationBase.SCALING);
					value = new Value(valInt);
				} else if (DatapointType.EIGHT_BIT_UNSIGNED_ANGLE.getTypeId().equals(type.getTypeId())) {
					valInt = communicator.readUnsigned(groupAddress, ProcessCommunicationBase.ANGLE);
					value = new Value(valInt);
				} else {
					valInt = communicator.readUnsigned(groupAddress, ProcessCommunicationBase.UNSCALED);
					value = new Value(rawToPercentage(valInt));
				}
				valueType = ValueType.NUMBER;
			} else if (dpt instanceof DPTSceneNumber || dpt instanceof DPTSceneControl) {
				Datapoint dataPnt = new StateDP(groupAddress, "1 byte scene", 0, dpt.getDtpId());
				valString = communicator.read(dataPnt);
				byte[] b = valString.getBytes();
				short number = (short) (b[0] & 0x3F);
				value = new Value(number);
				valueType = ValueType.NUMBER;
			} else if (dpt instanceof DPT2ByteUnsigned) {
				Datapoint dataPnt = new StateDP(groupAddress, "2 byte unsigned", 0, dpt.getDtpId());
				valString = communicator.read(dataPnt);
				byte[] b = valString.getBytes();
				int unsigned = ((b[0] << 8) & 0x0000ff00) | (b[1] & 0x000000ff);
				value = new Value(unsigned);
				valueType = ValueType.NUMBER;
			} else if (dpt instanceof DPT2ByteFloat) {
				valFloat = communicator.readFloat(groupAddress, false);
				value = new Value(valFloat);
				valueType = ValueType.NUMBER;
				unit = dpt.getUnit();
			} else if (dpt instanceof DPT4ByteFloat) {
				valFloat = communicator.readFloat(groupAddress, true);
				value = new Value(valFloat);
				valueType = ValueType.NUMBER;
			} else if (dpt instanceof DPTTime) {
				Datapoint dataPnt = new StateDP(groupAddress, "time", 0, dpt.getDtpId());
				valString = communicator.read(dataPnt);
				value = new Value(valString);
				valueType = ValueType.STRING;
			} else if (dpt instanceof DPTDate) {
				Datapoint dataPnt = new StateDP(groupAddress, "date", 0, dpt.getDtpId());
				valString = communicator.read(dataPnt);
				value = new Value(valString);
				valueType = ValueType.STRING;
			} else if (dpt instanceof DPTRGB) {
				Datapoint dataPnt = new StateDP(groupAddress, "rgb", 0, dpt.getDtpId());
				valString = communicator.read(dataPnt);
				value = new Value(valString);
				valueType = ValueType.STRING;
			} else if (dpt instanceof DPTDateTime) {
				Datapoint dataPnt = new StateDP(groupAddress, "datetime", 0, dpt.getDtpId());
				valString = communicator.read(dataPnt);
				value = new Value(valString);
				valueType = ValueType.STRING;
			} else if (dpt instanceof DPT64BitSigned) {
				Datapoint dataPnt = new StateDP(groupAddress, "64 bit signed", 0, dpt.getDtpId());
				valString = communicator.read(dataPnt);
				value = new Value(valString);
				valueType = ValueType.STRING;
			} else if (dpt instanceof DPTString) {
				valString = communicator.readString(groupAddress);
				value = new Value(valString);
				valueType = ValueType.STRING;
			} else {
				valString = type.getNameKey();
				valueType = ValueType.STRING;
			}

			pointNode.setValueType(valueType);
			pointNode.setValue(value);
			if (null != unit) {
				pointNode.setAttribute(ATTR_UNIT, new Value(unit));
			}
			addressToPolled.put(address, true);
			if (value != null) {
				LOGGER.debug("read and updated " + pointNode.getName() + " : " + value.toString());
			}
		} catch (KNXException | InterruptedException e) {
			LOGGER.debug("error: ", e.getMessage());
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
					readPoint(group, point);
				}
			}
		}
	}

	public void setPointValue(EditablePoint point, Value val) {

		DatapointType type = point.getType();
		GroupAddress dst = point.getGroupAddress();
		DPT dpt = type.getDpt();
		try {
			if (dpt instanceof DPTBoolean) {
				communicator.write(dst, val.getBool());
			} else if (dpt instanceof DPT1BitControlled) {
				Datapoint dataPnt = new StateDP(dst, "1 bit controlled", 0, dpt.getDtpId());
				communicator.write(dataPnt, val.getString());
			} else if (dpt instanceof DPT3BitControlled) {
				int stepcode = val.getNumber().intValue();
				boolean control = stepcode > 0 ? true : false;
				communicator.write(dst, control, Math.abs(stepcode));
			} else if (dpt instanceof DPT8BitUnsigned) {
				int unsigned = 0;
				if (DatapointType.EIGHT_BIT_UNSIGNED_SCALING.getTypeId().equals(type.getTypeId())) {
					unsigned = val.getNumber().intValue();
					communicator.write(dst, unsigned, ProcessCommunicationBase.SCALING);
				} else if (DatapointType.EIGHT_BIT_UNSIGNED_ANGLE.getTypeId().equals(type.getTypeId())) {
					unsigned = val.getNumber().intValue();
					communicator.write(dst, unsigned, ProcessCommunicationBase.ANGLE);
				} else {
					unsigned = val.getNumber().intValue();
					communicator.write(dst, percentageToRaw(unsigned), ProcessCommunicationBase.UNSCALED);
				}
			} else if (dpt instanceof DPTSceneNumber || dpt instanceof DPTSceneControl) {
				Datapoint dataPnt = new StateDP(dst, "1 byte scene", 0, dpt.getDtpId());
				communicator.write(dataPnt, val.getString());
			} else if (dpt instanceof DPT2ByteUnsigned) {
				Datapoint dataPnt = new StateDP(dst, "two byte unsigned", 0, dpt.getDtpId());
				communicator.write(dataPnt, val.getString());
			} else if (dpt instanceof DPT2ByteFloat) {
				communicator.write(dst, val.getNumber().floatValue(), false);
			} else if (dpt instanceof DPT4ByteFloat) {
				communicator.write(dst, val.getNumber().floatValue(), true);
			} else if (dpt instanceof DPTTime) {
				Datapoint dataPnt = new StateDP(dst, "time", 0, dpt.getDtpId());
				communicator.write(dataPnt, val.getString());
			} else if (dpt instanceof DPTDate) {
				Datapoint dataPnt = new StateDP(dst, "date", 0, dpt.getDtpId());
				communicator.write(dataPnt, val.getString());
			} else if (dpt instanceof DPTRGB) {
				Datapoint dataPnt = new StateDP(dst, "rgb", 0, dpt.getDtpId());
				communicator.write(dataPnt, val.getString());
			} else if (dpt instanceof DPTDateTime) {
				Datapoint dataPnt = new StateDP(dst, "datetime", 0, dpt.getDtpId());
				communicator.write(dataPnt, val.getString());
			} else if (dpt instanceof DPT64BitSigned) {
				Datapoint dataPnt = new StateDP(dst, "64 bit signed", 0, dpt.getDtpId());
				communicator.write(dataPnt, val.getString());
			} else if (dpt instanceof DPTString) {
				communicator.write(dst, val.getString());
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
		} else if (null != individualAddress && null != medium && null != restType) {
			LOGGER.debug("gateways already is created!");
		} else if (null == child.getAction() && !NODE_STATUS.equals(child.getName())) {
			node.removeChild(child);
		}
	}

	abstract KNXNetworkLink createLink();

	abstract void makeEditAction();
}
