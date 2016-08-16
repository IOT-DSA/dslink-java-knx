package org.dsa.iot.knx.project;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.dsa.iot.knx.EditableFolder;
import org.dsa.iot.knx.datapoint.DatapointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.GroupAddress;

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

	static final String DATA_TYPE_PREFIX_BOOLING = "BOOLEAN";
	static final String DATA_TYPE_PREFIX_CONTROL = "CONTROL";
	static final String DATA_TYPE_PREFIX_EIGHT_BIT_UNSIGNED = "EIGHTBITUNSIGNED";
	static final String DATA_TYPE_PREFIX_TWO_BYTE_UNSIGNED = "TWOBYTEUNSIGNED";
	static final String DATA_TYPE_PREFIX_TWO_BYTE_FLOAT = "TWOBYTEFLOAT";
	static final String DATA_TYPE_PREFIX_FOUR_BYTE_UNSIGNED = "FOURBYTEUNSIGNED";
	static final String DATA_TYPE_PREFIX_FOUR_BYTE_SIGNED = "FOURBYTESIGNED";
	static final String DATA_TYPE_PREFIX_FOUR_BYTE_FLOAT = "FOURBYTEFLOAT";
	static final String DATA_TYPE_PREFIX_STRING = "STRING";

	static final String DATA_TYPE_SHORTNAME_BOOLING = "boolean";
	static final String DATA_TYPE_SHORTNAME_CONTROL = "control";
	static final String DATA_TYPE_SHORTNAME_EIGHT_BIT_UNSIGNED = "8bitu";
	static final String DATA_TYPE_SHORTNAME_TWO_BYTE_UNSIGNED = "2byteu";
	static final String DATA_TYPE_SHORTNAME_TWO_BYTE_FLOAT = "2bytef";
	static final String DATA_TYPE_SHORTNAME_FOUR_BYTE_UNSIGNED = "4byteu";
	static final String DATA_TYPE_SHORTNAME_FOUR_BYTE_SIGNED = "4byte";
	static final String DATA_TYPE_SHORTNAME_FOUR_BYTE_FLOAT = "4bytef";
	static final String DATA_TYPE_SHORTNAME_STRING = "string";
	static final String DATA_TYPE_SHORTNAME_UNDEFINED = "undefined";

	Map<String, String> addressRefIdToTypeId;
	Map<String, String> addressRefIdToDescription;

	public EtsXmlParser(EditableFolder folder) {
		super(folder);

		addressRefIdToTypeId = new HashMap<>();
	}

	public void parseItems(String content) {
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
						// handle topology
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
															addressRefIdToDescription.put(groupAddressRefId,
																	description);
														}
													}
												}

											}
										}
									}
								}
							}
						}

						// handle group addresses
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
										List<GroupRange.GroupAddress> subGroupAddressList = middleRange
												.getGroupAddress();
										for (GroupRange.GroupAddress subGroupAddress : subGroupAddressList) {
											String rawAddressStr = subGroupAddress.getAddress();
											int rawAddress = Integer.parseInt(rawAddressStr);

											GroupAddress groupAddress = null;
											groupAddress = new GroupAddress(rawAddress);
											String addressRefId = subGroupAddress.getId();
											String typeId = addressRefIdToTypeId.get(addressRefId);

											String subGroupName = subGroupAddress.getName();
											int buildingIndex = subGroupName.indexOf(BUILDING);
											String path = subGroupName.substring(buildingIndex);
											String dataPointName = subGroupName.substring(0, buildingIndex - 1);
											String dataPointType = getDataPointTypeByTypeId(typeId);

											buildAddressToBean(mainGroupName, middleGroupName, groupAddress,
													dataPointType, dataPointName);

											if (!pathToNodes.containsKey(path)) {
												List<GroupAddress> nodes = new ArrayList<>();
												nodes.add(groupAddress);
												pathToNodes.put(path, nodes);
											} else {
												List<GroupAddress> nodes = pathToNodes.get(path);
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
	}

	private String getDataPointTypeByTypeId(String typeId) {
		DatapointType type = DatapointType.forTypeId(typeId);
		String dataPointTypeNme = type.toString();
		if (dataPointTypeNme.startsWith(DATA_TYPE_PREFIX_BOOLING)) {
			return DATA_TYPE_SHORTNAME_BOOLING;
		} else if (dataPointTypeNme.startsWith(DATA_TYPE_PREFIX_CONTROL)) {
			return DATA_TYPE_SHORTNAME_CONTROL;
		} else if (dataPointTypeNme.startsWith(DATA_TYPE_PREFIX_EIGHT_BIT_UNSIGNED)) {
			return DATA_TYPE_SHORTNAME_EIGHT_BIT_UNSIGNED;
		} else if (dataPointTypeNme.startsWith(DATA_TYPE_PREFIX_TWO_BYTE_UNSIGNED)) {
			return DATA_TYPE_SHORTNAME_TWO_BYTE_UNSIGNED;
		} else if (dataPointTypeNme.startsWith(DATA_TYPE_PREFIX_TWO_BYTE_FLOAT)) {
			return DATA_TYPE_SHORTNAME_TWO_BYTE_FLOAT;
		} else if (dataPointTypeNme.startsWith(DATA_TYPE_PREFIX_FOUR_BYTE_UNSIGNED)) {
			return DATA_TYPE_SHORTNAME_FOUR_BYTE_UNSIGNED;
		} else if (dataPointTypeNme.startsWith(DATA_TYPE_PREFIX_FOUR_BYTE_SIGNED)) {
			return DATA_TYPE_SHORTNAME_FOUR_BYTE_SIGNED;
		} else if (dataPointTypeNme.startsWith(DATA_TYPE_PREFIX_FOUR_BYTE_FLOAT)) {
			return DATA_TYPE_SHORTNAME_FOUR_BYTE_FLOAT;
		} else if (dataPointTypeNme.startsWith(DATA_TYPE_PREFIX_STRING)) {
			return DATA_TYPE_SHORTNAME_STRING;
		} else {
			return DATA_TYPE_SHORTNAME_UNDEFINED;
		}
	}
}
