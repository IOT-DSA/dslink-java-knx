package org.dsa.iot.knx.project;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.dsa.iot.knx.EditableFolder;
import org.dsa.iot.knx.datapoint.DatapointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hua hou
 */

public class EtsXmlParser extends KnxProjectParser {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(OpcFileParser.class);
	}

	static final String DATA_TYPE_SHORTNAME_UNDEFINED = "undefined";

	static final String VERSION_SEPARATOR = "-";

	Map<String, String> addressRefIdToTypeId;
	Map<String, Integer> dataPointTypeIdToSizeInBit = new HashMap<>();

	public Map<String, Integer> getDataPointTypeIdToSize() {
		return dataPointTypeIdToSizeInBit;
	}

	public void setDataPointTypeIdToSize(Map<String, Integer> dataPointTypeIdToSize) {
		this.dataPointTypeIdToSizeInBit = dataPointTypeIdToSize;
	}

	public Map<String, String> getDataPointSubTypeIdToSubTypeName() {
		return dataPointSubTypeIdToSubTypeName;
	}

	public void setDataPointSubTypeIdToSubTypeName(Map<String, String> dataPointSubTypeIdToSubTypeName) {
		this.dataPointSubTypeIdToSubTypeName = dataPointSubTypeIdToSubTypeName;
	}

	public Map<String, String> getDataPointSubTypeIdToDataPointTypeId() {
		return dataPointSubTypeIdToDataPointTypeId;
	}

	public void setDataPointSubTypeIdToDataPointTypeId(Map<String, String> dataPointSubTypeIdToDataPointTypeId) {
		this.dataPointSubTypeIdToDataPointTypeId = dataPointSubTypeIdToDataPointTypeId;
	}

	Map<String, String> dataPointSubTypeIdToSubTypeName = new HashMap<>();
	Map<String, String> dataPointSubTypeIdToDataPointTypeId = new HashMap<>();

	public EtsXmlParser(EditableFolder folder) {
		super(folder);

		addressRefIdToTypeId = new HashMap<>();
	}

	public void parse(String content) {
		LOGGER.info("start parsing project xml");
		JAXBContext context = null;
		try {
			context = JAXBContext.newInstance(KNX.class);
			Unmarshaller unMarshaller = context.createUnmarshaller();
			InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			KNX knx = (KNX) unMarshaller.unmarshal(stream);

			List<KNX.Project> projectList = knx.getProject();
			for (KNX.Project project : projectList) {
				List<KNX.Project.Installations> installationsList = project.getInstallations();
				for (KNX.Project.Installations installations : installationsList) {
					List<KNX.Project.Installations.Installation> installationList = installations.getInstallation();
					for (KNX.Project.Installations.Installation installation : installationList) {
						LOGGER.info("parsing topology......");
						List<KNX.Project.Installations.Installation.Topology> topologyList = installation.getTopology();
						for (KNX.Project.Installations.Installation.Topology topology : topologyList) {
							List<KNX.Project.Installations.Installation.Topology.Area> areaList = topology.getArea();
							for (KNX.Project.Installations.Installation.Topology.Area area : areaList) {
								List<KNX.Project.Installations.Installation.Topology.Area.Line> lineList = area
										.getLine();
								for (KNX.Project.Installations.Installation.Topology.Area.Line line : lineList) {
									List<KNX.Project.Installations.Installation.Topology.Area.Line.DeviceInstance> deviceList = line
											.getDeviceInstance();
									for (KNX.Project.Installations.Installation.Topology.Area.Line.DeviceInstance device : deviceList) {
										List<KNX.Project.Installations.Installation.Topology.Area.Line.DeviceInstance.ComObjectInstanceRefs> ComObjectInstanceRefsList = device
												.getComObjectInstanceRefs();
										for (KNX.Project.Installations.Installation.Topology.Area.Line.DeviceInstance.ComObjectInstanceRefs ComObjectInstanceRefs : ComObjectInstanceRefsList) {
											List<KNX.Project.Installations.Installation.Topology.Area.Line.DeviceInstance.ComObjectInstanceRefs.ComObjectInstanceRef> comObjectInstanceRefList = ComObjectInstanceRefs
													.getComObjectInstanceRef();
											for (KNX.Project.Installations.Installation.Topology.Area.Line.DeviceInstance.ComObjectInstanceRefs.ComObjectInstanceRef comOjbectInstanceRef : comObjectInstanceRefList) {
												String dataTypeId = comOjbectInstanceRef.getDatapointType();
												String description = comOjbectInstanceRef.getDescription();
												List<KNX.Project.Installations.Installation.Topology.Area.Line.DeviceInstance.ComObjectInstanceRefs.ComObjectInstanceRef.Connectors> connectorsList = comOjbectInstanceRef
														.getConnectors();
												for (KNX.Project.Installations.Installation.Topology.Area.Line.DeviceInstance.ComObjectInstanceRefs.ComObjectInstanceRef.Connectors connectors : connectorsList) {
													List<KNX.Project.Installations.Installation.Topology.Area.Line.DeviceInstance.ComObjectInstanceRefs.ComObjectInstanceRef.Connectors.Send> sendList = connectors
															.getSend();
													for (KNX.Project.Installations.Installation.Topology.Area.Line.DeviceInstance.ComObjectInstanceRefs.ComObjectInstanceRef.Connectors.Send send : sendList) {
														String groupAddressRefId = send.getGroupAddressRefId();
														if (null != dataTypeId && null != groupAddressRefId) {
															addressRefIdToTypeId.put(groupAddressRefId, dataTypeId);
														}
													}
												}

											}
										}
									}
								}
							}
						}

						LOGGER.info("parsing building parts......");
						List<KNX.Project.Installations.Installation.Buildings> buildingsList = installation
								.getBuildings();
						for (KNX.Project.Installations.Installation.Buildings buildings : buildingsList) {
							// TBD
						}

						LOGGER.info("parsing group addresses......");
						List<KNX.Project.Installations.Installation.GroupAddresses> groupAddressesList = installation
								.getGroupAddresses();
						for (KNX.Project.Installations.Installation.GroupAddresses groupAddresses : groupAddressesList) {
							List<KNX.Project.Installations.Installation.GroupAddresses.GroupRanges> groupRangeList = groupAddresses
									.getGroupRanges();
							for (KNX.Project.Installations.Installation.GroupAddresses.GroupRanges ranges : groupRangeList) {
								List<GroupRange> mainRangeList = ranges.getGroupRange();
								for (GroupRange mainRange : mainRangeList) {
									this.mainGroupName = mainRange.getName();
									List<GroupRange> middleRangeList = mainRange.getGroupRange();
									for (GroupRange middleRange : middleRangeList) {
										this.middleGroupName = middleRange.getName();
										List<GroupAddress> subGroupAddressList = middleRange.getGroupAddress();
										for (GroupAddress subGroupAddress : subGroupAddressList) {
											String rawAddressStr = subGroupAddress.getAddress();
											int rawAddress = Integer.parseInt(rawAddressStr);

											tuwien.auto.calimero.GroupAddress groupAddress = null;
											groupAddress = new tuwien.auto.calimero.GroupAddress(rawAddress);
											String addressRefId = subGroupAddress.getId();
											String typeId = addressRefIdToTypeId.get(addressRefId);
											if (null != typeId) {
												String subGroupName = subGroupAddress.getName();
												String[] dataPointAndPath = parseGroupAddress(subGroupName);
												String dataPointName = dataPointAndPath[0];
												String path = dataPointAndPath[1];

												String dataPointType = getDataPointTypeByTypeId(typeId);

												buildAddressToBean(mainGroupName, middleGroupName, groupAddress,
														dataPointType, dataPointName);

												if (!pathToNodes.containsKey(path)) {
													List<tuwien.auto.calimero.GroupAddress> nodes = new ArrayList<>();
													nodes.add(groupAddress);
													pathToNodes.put(path, nodes);
												} else {
													List<tuwien.auto.calimero.GroupAddress> nodes = pathToNodes
															.get(path);
													nodes.add(groupAddress);
													pathToNodes.put(path, nodes);
												}
											}
										}
									}
								}
							}
						}
					}
				}
				// build the folder tree from the hashMap
				buildGroupTree();
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null == context) {
				return;
			}
		}
		LOGGER.info("parsing is done!");
	}

	private String getDataPointTypeByTypeId(String typeId) {
		DatapointType type = null;
		if (typeId.startsWith("DPT")) {
			typeId = typeId.replaceFirst("DPT", "DPST");
			type = DatapointType.forMajorTypeId(typeId, true);
		} else {
			type = DatapointType.forTypeId(typeId);
		}

		String dataPointTypeNme = type.toString();
		return dataPointTypeNme;
	}
}
