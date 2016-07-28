package org.dsa.iot.knx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.dslink.util.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
import tuwien.auto.calimero.knxnetip.util.DeviceDIB;
import tuwien.auto.calimero.knxnetip.util.HPAI;
import tuwien.auto.calimero.knxnetip.util.ServiceFamiliesDIB;

public class KnxIpConnection extends KnxConnection {
	static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(KnxIpConnection.class);
	}

	static final String ACTION_ADD_DEVICE = "add device";
	static final String ACTION_DISCOVER_DEVICES = "discover devices";
	static final String ATTR_USE_NAT = "use NAT";
	static final int SEARCH_TIMEOUT = 5;
	static final int DEFAULT_INTERVAL = 5;

	boolean useNat = false;
	static ScheduledThreadPoolExecutor stpe;
	static ScheduledFuture<?> future;

	Map<String, HPAI> addressToHPAI;
	Map<String, DeviceDIB> addressToDeviceDIB;
	Map<String, ServiceFamiliesDIB> addressToServiceFamiliesDIB;

	public KnxIpConnection(KnxLink link, Node node) {
		super(link, node);

		useNat = node.getAttribute(ATTR_USE_NAT).getBool();
		stpe = Objects.createDaemonThreadPool();
		addressToDeviceDIB = new HashMap<String, DeviceDIB>();
	}

	public void init() {
		makeEditAction();
		makeRemoveAction();
		makeDiscoverAction();
	}

	public KnxConnection getConnection() {
		return this;
	}

	public void makeDiscoverAction() {
		Action act = new Action(Permission.READ, new DeviceDiscoveryHandler());
		Node actionNode = node.getChild(ACTION_DISCOVER_DEVICES);
		if (actionNode == null)
			node.createChild(ACTION_DISCOVER_DEVICES).setAction(act).build().setSerializable(false);
		else
			actionNode.setAction(act);
	}

	private class DeviceDiscoveryHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			discover();
		}

	}

	private class Discover implements Runnable {
		KnxConnection listener;

		public void addListener(KnxConnection listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			Discoverer discoverer;
			try {
				discoverer = new Discoverer(null, 0, false, true);
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
					this.listener.onDiscovered();
				}

				if (responses.length > 0) {
					future.cancel(true);
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
		future = stpe.scheduleWithFixedDelay(discover, 0, getInterval(), TimeUnit.MILLISECONDS);
	}

	protected void setupDeviceNode(String host, DeviceDIB dib) {
		String name = dib.getName();
		Node child = node.createChild(name).build();
		DeviceNode deviceNode = new DeviceNode(getConnection(), null, child);
	}

	ScheduledThreadPoolExecutor getDaemonThreadPool() {
		return stpe;
	}

	int getInterval() {
		return DEFAULT_INTERVAL;
	}

	@Override
	public void onDiscovered() {
		Iterator it = addressToDeviceDIB.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String host = (String) pair.getKey();
			DeviceDIB dib = (DeviceDIB) pair.getValue();
			setupDeviceNode(host, dib);
		}
	}
}
