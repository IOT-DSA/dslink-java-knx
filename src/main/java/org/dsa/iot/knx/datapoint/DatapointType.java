package org.dsa.iot.knx.datapoint;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public enum DatapointType {
    UNDEFINED("knx.dpt.undefined", "", new DPTUndefined()), //

    //
    //
    // Boolean (1)
    //
    BOOLEAN_SWITCH("knx.dpt.boolean.switch", "DPST-1-1", new DPTBoolean("1.001", "Off", "On")), //
    BOOLEAN_BOOLEAN("knx.dpt.boolean.boolean", "DPST-1-2", new DPTBoolean("1.002", "False", "True")), //
    BOOLEAN_ENABLE("knx.dpt.boolean.enable", "DPST-1-3", new DPTBoolean("1.003", "Disable", "Enable")), //
    BOOLEAN_RAMP("knx.dpt.boolean.ramp", "DPST-1-4", new DPTBoolean("1.004", "No ramp", "Ramp")), //
    BOOLEAN_ALARM("knx.dpt.boolean.alarm", "DPST-1-5", new DPTBoolean("1.005", "No alarm", "Alarm")), //
    BOOLEAN_BINARYVALUE("knx.dpt.boolean.binaryValue", "DPST-1-6", new DPTBoolean("1.006", "Low", "High")), //
    BOOLEAN_STEP("knx.dpt.boolean.step", "DPST-1-7", new DPTBoolean("1.007", "Decrease", "Increase")), //
    BOOLEAN_UPDOWN("knx.dpt.boolean.upDown", "DPST-1-8", new DPTBoolean("1.008", "Up", "Down")), //
    BOOLEAN_OPENCLOSE("knx.dpt.boolean.openClose", "DPST-1-9", new DPTBoolean("1.009", "Open", "Close")), //
    BOOLEAN_START("knx.dpt.boolean.start", "DPST-1-10", new DPTBoolean("1.010", "Stop", "Start")), //
    BOOLEAN_STATE("knx.dpt.boolean.state", "DPST-1-11", new DPTBoolean("1.011", "Inactive", "Active")), //
    BOOLEAN_INVERT("knx.dpt.boolean.invert", "DPST-1-12", new DPTBoolean("1.012", "Not inverted", "Inverted")), //
    BOOLEAN_DIMSENDSTYLE("knx.dpt.boolean.dimSendStyle", "DPST-1-13", new DPTBoolean("1.013", "Start/stop",
            "Cyclically")), //
    BOOLEAN_INPUTSOURCE("knx.dpt.boolean.inputSource", "DPST-1-14", new DPTBoolean("1.014", "Fixed", "Calculated")), //
    BOOLEAN_RESET("knx.dpt.boolean.reset", "DPST-1-15", new DPTBoolean("1.015", "No action", "Reset")), //
    BOOLEAN_ACK("knx.dpt.boolean.ack", "DPST-1-16", new DPTBoolean("1.016", "No action", "Acknowledge")), //
    BOOLEAN_TRIGGER("knx.dpt.boolean.trigger", "DPST-1-17", new DPTBoolean("1.017", "Trigger", "Trigger+")), //
    BOOLEAN_OCCUPANCY("knx.dpt.boolean.occupancy", "DPST-1-18", new DPTBoolean("1.018", "Not occupied", "Occupied")), //
    BOOLEAN_WINDOWDOOR("knx.dpt.boolean.windowDoor", "DPST-1-19", new DPTBoolean("1.019", "Closed", "Open")), //
    BOOLEAN_LOGICALFUNCTION("knx.dpt.boolean.logicalFunction", "DPST-1-21", new DPTBoolean("1.021", "OR", "AND")), //
    BOOLEAN_SCENE("knx.dpt.boolean.scene", "DPST-1-22", new DPTBoolean("1.022", "Scene A", "Scene B")), //
    BOOLEAN_SHUTTERBLINDSMODE("knx.dpt.boolean.shutterBlindsMode", "DPST-1-23", new DPTBoolean("1.023",
            "Only move up/down", "Move up/down + step-stop")), //
    //BOOLEAN_HEATBOOL("knx.dpt.boolean.heatCool", new DPTBoolean("1.100", "Cooling", "Heating")), //

 ;

    public final String nameKey;
    public final String typeId;
    public final DPT dpt;

    private DatapointType(String nameKey, String typeId, DPT dpt) {
        this.nameKey = nameKey;
        this.typeId = typeId;
        this.dpt = dpt;
    }

    public static DatapointType forTypeId(String typeId) {
        for (DatapointType e : values()) {
            if (e.typeId.equals(typeId))
                return e;
        }
        return UNDEFINED;
    }

    public static Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (DatapointType t : values()) {
            Map<String, Object> tm = new HashMap<>();
            tm.put("labelKey", t.nameKey);
            tm.put("dataTypeId", t.dpt.getDataTypeId());
            result.put(t.name(), tm);
        }
        return result;
    }
}
