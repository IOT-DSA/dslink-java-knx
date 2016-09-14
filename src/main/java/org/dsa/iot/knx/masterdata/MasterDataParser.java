/**
 *
 * @author hua hou
 */

package org.dsa.iot.knx.masterdata;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class MasterDataParser {
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(MasterDataParser.class);
	}

	Map<String, Integer> dataPointSubTypeIdToSize = new HashMap<>();
	Map<String, String> dataPointSubTypeIdToSubTypeName = new HashMap<>();

	public void parse(String content) {

		JAXBContext context = null;
		try {
			context = JAXBContext.newInstance(KNX.class);
			Unmarshaller unMarshaller = context.createUnmarshaller();
			InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			KNX knx = (KNX) unMarshaller.unmarshal(stream);
			List<Object> objList = knx.getLocationOrAccessRightsOrLanguage();
			for (Object obj : objList) {
				KNX.MasterData master = (KNX.MasterData) obj;
				List<KNX.MasterData.DatapointTypes> dataPointTypesList = master.getDatapointTypes();
				for (KNX.MasterData.DatapointTypes dataPointTypes : dataPointTypesList) {
					List<KNX.MasterData.DatapointTypes.DatapointType> dataPointTypeList = dataPointTypes
							.getDatapointType();
					for (KNX.MasterData.DatapointTypes.DatapointType dpt : dataPointTypeList) {
						String dataPointTypeId = dpt.getId();
						Integer sizeInBit = Integer.parseInt(dpt.getSizeInBit());
						dataPointSubTypeIdToSize.put(dataPointTypeId, sizeInBit);
						LOGGER.info(dataPointTypeId);
						List<KNX.MasterData.DatapointTypes.DatapointType.DatapointSubtypes> subDataPointTypesList = dpt
								.getDatapointSubtypes();
						for (KNX.MasterData.DatapointTypes.DatapointType.DatapointSubtypes subTypes : subDataPointTypesList) {
							List<KNX.MasterData.DatapointTypes.DatapointType.DatapointSubtypes.DatapointSubtype> subTypeList = subTypes
									.getDatapointSubtype();
							for (KNX.MasterData.DatapointTypes.DatapointType.DatapointSubtypes.DatapointSubtype subType : subTypeList) {
								String subTypeId = subType.getId();
								String subTypeName = subType.getName();
								LOGGER.info(subTypeId + " : " + subTypeName);
								dataPointSubTypeIdToSubTypeName.put(subTypeId, subTypeName);
							}
						}

					}
				}

			}
			LOGGER.info(knx.toString());

		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
