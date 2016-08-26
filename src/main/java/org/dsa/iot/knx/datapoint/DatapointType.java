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
    //
    //
    // 3 bit controlled (3)
    
    //   CONTROL_DIMMING("knx.dpt.control.dimming", DPTXlator3BitControlled.class, "3.007", null), //
    //   CONTROL_BLINDS("knx.dpt.control.blinds", DPTXlator3BitControlled.class, "3.008", null), //

    //
    //
    // 8 bit unsigned (5)
    //
    EIGHTBITUNSIGNED_SCALING("knx.dpt.8bitu.scaling", "DPST-5-1", new DPT8BitUnsigned("5.001", "#.#", "%")), //
    EIGHTBITUNSIGNED_ANGLE("knx.dpt.8bitu.angle", "DPST-5-3", new DPT8BitUnsigned("5.003", "#.#", "\u00b0")), //
    EIGHTBITUNSIGNED_PERCENT("knx.dpt.8bitu.percent", "DPST-5-4", new DPT8BitUnsigned("5.004", "#.#", "%")), //
    EIGHTBITUNSIGNED_DECIMALFACTOR("knx.dpt.8bitu.decimalFactor", "DPST-5-5", new DPT8BitUnsigned("5.005", "#", "")), //
    EIGHTBITUNSIGNED_COUNT("knx.dpt.8bitu.count", "DPST-5-10", new DPT8BitUnsigned("5.010", "#", "pulses")), //

    //
    //
    // 2 byte unsigned (7)
    //
    TWOBYTEUNSIGNED_SCALING("knx.dpt.2byteu.count", "DPST-7-1", new DPT2ByteUnsigned("7.001", "#", "pulses")), //
    TWOBYTEUNSIGNED_TIMEPERIOD("knx.dpt.2byteu.timePeriod", "DPST-7-2", new DPT2ByteUnsigned("7.002", "#", "ms")), //
    TWOBYTEUNSIGNED_TIMEPERIOD10("knx.dpt.2byteu.timePeriod10", "DPST-7-3", new DPT2ByteUnsigned("7.003", "#", "ms")), //
    TWOBYTEUNSIGNED_TIMEPERIOD100("knx.dpt.2byteu.timePeriod100", "DPST-7-4", new DPT2ByteUnsigned("7.004", "#", "ms")), //
    TWOBYTEUNSIGNED_TIMEPERIODSEC("knx.dpt.2byteu.timePeriodSec", "DPST-7-5", new DPT2ByteUnsigned("7.005", "#", "s")), //
    TWOBYTEUNSIGNED_TIMEPERIODMIN("knx.dpt.2byteu.timePeriodMin", "DPST-7-6", new DPT2ByteUnsigned("7.006", "#", "m")), //
    TWOBYTEUNSIGNED_TIMEPERIODHOUR("knx.dpt.2byteu.timePeriodHour", "DPST-7-1", new DPT2ByteUnsigned("7.007", "#", "h")), //
    TWOBYTEUNSIGNED_PROPDATATYPE("knx.dpt.2byteu.propDataType", "DPST-7-10", new DPT2ByteUnsigned("7.010", "#", "")), //
    TWOBYTEUNSIGNED_ELECTRICALCURRENT("knx.dpt.2byteu.electricalCurrent", "DPST-7-12", new DPT2ByteUnsigned("7.012",
            "#", "mA")), //
    TWOBYTEUNSIGNED_BRIGHTNESS("knx.dpt.2byteu.brightness", "DPST-7-13", new DPT2ByteUnsigned("7.013", "#", "lx")), //

    //
    //
    // 2 byte float (9)
    //
    TWOBYTEFLOAT_TEMPERATURE("knx.dpt.2bytef.temperature", "DPST-9-1", new DPT2ByteFloat("9.001", "#.##", "\u00b0C")), //
    TWOBYTEFLOAT_TEMPDIFF("knx.dpt.2bytef.tempDiff", "DPST-9-2", new DPT2ByteFloat("9.002", "#.##", "K")), //
    TWOBYTEFLOAT_GRADIENT("knx.dpt.2bytef.gradient", "DPST-9-3", new DPT2ByteFloat("9.003", "#.##", "K/h")), //
    TWOBYTEFLOAT_INTENSITYOFLIGHT("knx.dpt.2bytef.intensityOfLight", "DPST-9-4", new DPT2ByteFloat("9.004", "#.##",
            "lx")), //
    TWOBYTEFLOAT_WINDSPEED("knx.dpt.2bytef.windSpeed", "DPST-9-5", new DPT2ByteFloat("9.005", "#.##", "m/s")), //
    TWOBYTEFLOAT_AIRPRESSURE("knx.dpt.2bytef.airPressure", "DPST-9-6", new DPT2ByteFloat("9.006", "#.##", "Pa")), //
    TWOBYTEFLOAT_HUMIDITY("knx.dpt.2bytef.humidity", "DPST-9-7", new DPT2ByteFloat("9.007", "#.##", "%")), //
    TWOBYTEFLOAT_AIRQUALITY("knx.dpt.2bytef.airQuality", "DPST-9-8", new DPT2ByteFloat("9.008", "#.##", "ppm")), //
    TWOBYTEFLOAT_TIMEDIFF1("knx.dpt.2bytef.timeDifference1", "DPST-9-10", new DPT2ByteFloat("9.010", "#.##", "s")), //
    TWOBYTEFLOAT_TIMEDIFF2("knx.dpt.2bytef.timeDifference2", "DPST-9-11", new DPT2ByteFloat("9.011", "#.##", "ms")), //
    TWOBYTEFLOAT_VOLTAGE("knx.dpt.2bytef.voltage", "DPST-9-20", new DPT2ByteFloat("9.020", "#.##", "mV")), //
    TWOBYTEFLOAT_ELECTRICALCURRENT("knx.dpt.2bytef.electricalCurrent", "DPST-9-21", new DPT2ByteFloat("9.021", "#.##",
            "mA")), //
    TWOBYTEFLOAT_POWERDENSITY("knx.dpt.2bytef.powerDensity", "DPST-9-22", new DPT2ByteFloat("9.022", "#.##",
            "W/m\u00b2")), //
    TWOBYTEFLOAT_KELVINPERPERCENT("knx.dpt.2bytef.kelvinPerPercent", "DPST-9-23", new DPT2ByteFloat("9.023", "#.##",
            "K/%")), //
    TWOBYTEFLOAT_POWER("knx.dpt.2bytef.power", "DPST-9-24", new DPT2ByteFloat("9.024", "#.##", "kW")), //

    //    //
    //    //
    //    // Date and time (10 and 11)
    //    //
    //    TWOBYTEFLOAT_POWER("knx.dpt.2bytef.power", "DPST-9-24", new DPT2ByteFloat("9.024", "#.##", "kW")), //

    //
    //
    // 4 byte unsigned (12)
    //
    FOURBYTEUNSIGNED_COUNT("knx.dpt.4byteu.count", "DPST-12-1", new DPT4ByteUnsigned("12.001", "#", "pulses")), //

    //
    //
    // 4 byte signed (13)
    //
    FOURBYTESIGNED_COUNT("knx.dpt.4byte.count", "DPST-13-1", new DPT4ByteSigned("13.001", "#", "pulses")), //
    FOURBYTESIGNED_ACTIVEENERGY("knx.dpt.4byte.activeEnergy", "DPST-13-10", new DPT4ByteSigned("13.010", "#", "Wh")), //
    FOURBYTESIGNED_REACTIVEENERGY("knx.dpt.4byte.reactiveEnergy", "DPST-13-12",
            new DPT4ByteSigned("13.012", "#", "Var")), //

    //
    //
    // 4 byte float (14)
    //
    FOURBYTEFLOAT_ACCELERATION("knx.dpt.4bytef.acceleration", "DPST-14-0", new DPT4ByteFloat("14.000", "#.#",
            "m/s\u00b2")), //
    FOURBYTEFLOAT_ACCELERATION_ANGULAR("knx.dpt.4bytef.angularAcceleration", "DPST-14-1", new DPT4ByteFloat("14.001",
            "#.#", "rad/s\u00b2")), //
    FOURBYTEFLOAT_ACTIVATION_ENERGY("knx.dpt.4bytef.activationEnergy", "DPST-14-2", new DPT4ByteFloat("14.002", "#.#",
            "J/mol")), //
    FOURBYTEFLOAT_ACTIVITY("knx.dpt.4bytef.activity", "DPST-14-3", new DPT4ByteFloat("14.003", "#.#", "1/s")), //
    FOURBYTEFLOAT_MOL("knx.dpt.4bytef.amountOfSubstance", "DPST-14-4", new DPT4ByteFloat("14.004", "#.#", "mol")), //
    FOURBYTEFLOAT_AMPLITUDE("knx.dpt.4bytef.amplitude", "DPST-14-5", new DPT4ByteFloat("14.005", "#.#", "")), //
    FOURBYTEFLOAT_ANGLE_RAD("knx.dpt.4bytef.radiant", "DPST-14-6", new DPT4ByteFloat("14.006", "#.#", "rad")), //
    FOURBYTEFLOAT_ANGLE_DEG("knx.dpt.4bytef.degree", "DPST-14-7", new DPT4ByteFloat("14.007", "#.#", "\u00b0")), //
    FOURBYTEFLOAT_ANGULAR_MOMENTUM("knx.dpt.4bytef.angularMomentum", "DPST-14-8", new DPT4ByteFloat("14.008", "#.#",
            "Js")), //
    FOURBYTEFLOAT_ANGULAR_VELOCITY("knx.dpt.4bytef.angularVelocity", "DPST-14-9", new DPT4ByteFloat("14.009", "#.#",
            "rad/s")), //
    FOURBYTEFLOAT_AREA("knx.dpt.4bytef.area", "DPST-14-10", new DPT4ByteFloat("14.010", "#.#", "m\u00b2")), //
    FOURBYTEFLOAT_CAPACITANCE("knx.dpt.4bytef.capacitance", "DPST-14-11", new DPT4ByteFloat("14.011", "#.#", "F")), //
    FOURBYTEFLOAT_CHARGE_DENSITY_SURFACE("knx.dpt.4bytef.chargeDensitySurface", "DPST-14-12", new DPT4ByteFloat(
            "14.012", "#.#", "C/m\u00b2")), //
    FOURBYTEFLOAT_CHARGE_DENSITY_VOLUME("knx.dpt.4bytef.chargeDensityVolume", "DPST-14-13", new DPT4ByteFloat("14.013",
            "#.#", "C/m\u00b3")), //
    FOURBYTEFLOAT_COMPRESSIBILITY("knx.dpt.4bytef.compressibility", "DPST-14-14", new DPT4ByteFloat("14.014", "#.#",
            "m\u00b2/N")), //
    FOURBYTEFLOAT_CONDUCTANCE("knx.dpt.4bytef.conductance", "DPST-14-15", new DPT4ByteFloat("14.015", "#.#", "S")), //
    FOURBYTEFLOAT_ELECTRICAL_CONDUCTIVITY("knx.dpt.4bytef.electricalConductivity", "DPST-14-16", new DPT4ByteFloat(
            "14.016", "#.#", "S/m")), //
    FOURBYTEFLOAT_DENSITY("knx.dpt.4bytef.density", "DPST-14-17", new DPT4ByteFloat("14.017", "#.#", "kg/m\u00b3")), //
    FOURBYTEFLOAT_ELECTRICAL_CHARGE("knx.dpt.4bytef.electricCharge", "DPST-14-18", new DPT4ByteFloat("14.018", "#.#",
            "C")), //
    FOURBYTEFLOAT_ELECTRICAL_CURRENT("knx.dpt.4bytef.electricCurrent", "DPST-14-19", new DPT4ByteFloat("14.019", "#.#",
            "A")), //
    FOURBYTEFLOAT_ELECTRICAL_CURRENT_DENSITY("knx.dpt.4bytef.electricCurrentDensity", "DPST-14-20", new DPT4ByteFloat(
            "14.020", "#.#", "A/m\u00b2")), //
    FOURBYTEFLOAT_ELECTRICAL_DIPOLE_MOMENT("knx.dpt.4bytef.electricDipoleMoment", "DPST-14-21", new DPT4ByteFloat(
            "14.021", "#.#", "Cm")), //
    FOURBYTEFLOAT_ELECTRICAL_DISPLACEMENT("knx.dpt.4bytef.electricDisplacement", "DPST-14-22", new DPT4ByteFloat(
            "14.022", "#.#", "C/m\u00b2")), //
    FOURBYTEFLOAT_ELECTRICAL_FIELD_STRENGTH("knx.dpt.4bytef.electricFieldStrength", "DPST-14-23", new DPT4ByteFloat(
            "14.023", "#.#", "V/m")), //
    FOURBYTEFLOAT_ELECTRICAL_FLUX("knx.dpt.4bytef.electricFlux", "DPST-14-24", new DPT4ByteFloat("14.024", "#.#", "c")), //
    FOURBYTEFLOAT_ELECTRICAL_FLUX_DENSITY("knx.dpt.4bytef.electricFluxDensity", "DPST-14-25", new DPT4ByteFloat(
            "14.025", "#.#", "C/m\u00b2")), //
    FOURBYTEFLOAT_ELECTRICAL_POLARIZATION("knx.dpt.4bytef.electricPolarization", "DPST-14-26", new DPT4ByteFloat(
            "14.026", "#.#", "C/m\u00b2")), //
    FOURBYTEFLOAT_ELECTRICAL_POTENTIAL("knx.dpt.4bytef.electricPotential", "DPST-14-27", new DPT4ByteFloat("14.027",
            "#.#", "V")), //
    FOURBYTEFLOAT_ELECTRICAL_POTENTIAL_DIFFERENCE("knx.dpt.4bytef.electricPotentialDifference", "DPST-14-28",
            new DPT4ByteFloat("14.028", "#.#", "V")), //
    FOURBYTEFLOAT_ELECTROMAGNETIC_MOMENT("knx.dpt.4bytef.electromagneticMoment", "DPST-14-29", new DPT4ByteFloat(
            "14.029", "#.#", "Am\u00b2")), //
    FOURBYTEFLOAT_ELECTROMOTIVE_FORCE("knx.dpt.4bytef.electromotiveForce", "DPST-14-30", new DPT4ByteFloat("14.030",
            "#.#", "V")), //
    FOURBYTEFLOAT_ENERGY("knx.dpt.4bytef.energy", "DPST-14-31", new DPT4ByteFloat("14.031", "#.#", "J")), //
    FOURBYTEFLOAT_FORCE("knx.dpt.4bytef.force", "DPST-14-32", new DPT4ByteFloat("14.032", "#.#", "N")), //
    FOURBYTEFLOAT_FREQUENCY("knx.dpt.4bytef.frequency", "DPST-14-33", new DPT4ByteFloat("14.033", "#.#", "Hz")), //
    FOURBYTEFLOAT_ANGULAR_FREQUENCY("knx.dpt.4bytef.angularFrequency", "DPST-14-34", new DPT4ByteFloat("14.034", "#.#",
            "rad/s")), //
    FOURBYTEFLOAT_HEAT_CAPACITY("knx.dpt.4bytef.heatCapacity", "DPST-14-35", new DPT4ByteFloat("14.035", "#.#", "J/K")), //
    FOURBYTEFLOAT_HEAT_FLOW_RATE("knx.dpt.4bytef.heatFlowRate", "DPST-14-36", new DPT4ByteFloat("14.036", "#.#", "W")), //
    FOURBYTEFLOAT_HEAT_QUANTITY("knx.dpt.4bytef.heatQuantity", "DPST-14-37", new DPT4ByteFloat("14.037", "#.#", "J")), //
    FOURBYTEFLOAT_IMPENDANCE("knx.dpt.4bytef.impendance", "DPST-14-38", new DPT4ByteFloat("14.038", "#.#", "\u03A9")), //
    FOURBYTEFLOAT_LENGTH("knx.dpt.4bytef.length", "DPST-14-39", new DPT4ByteFloat("14.039", "#.#", "m")), //
    FOURBYTEFLOAT_LIGHT_QUANTITY("knx.dpt.4bytef.lightQuantity", "DPST-14-40", new DPT4ByteFloat("14.040", "#.#", "J")), //
    FOURBYTEFLOAT_LUMINANCE("knx.dpt.4bytef.luminance", "DPST-14-41", new DPT4ByteFloat("14.041", "#.#", "cd/m\u00b2")), //
    FOURBYTEFLOAT_LUMINOUS_FLUX("knx.dpt.4bytef.luminousFlux", "DPST-14-42", new DPT4ByteFloat("14.042", "#.#", "lm")), //
    FOURBYTEFLOAT_LUMINOUS_INTENSITY("knx.dpt.4bytef.luminousIntensity", "DPST-14-43", new DPT4ByteFloat("14.043",
            "#.#", "cd")), //
    FOURBYTEFLOAT_MAGNETIC_FIELD_STRENGTH("knx.dpt.4bytef.magneticFieldStrength", "DPST-14-44", new DPT4ByteFloat(
            "14.044", "#.#", "A/m")), //
    FOURBYTEFLOAT_MAGNETIC_FLUX("knx.dpt.4bytef.magneticFlux", "DPST-14-45", new DPT4ByteFloat("14.045", "#.#", "Wb")), //
    FOURBYTEFLOAT_MAGNETIC_FLUX_DENSITY("knx.dpt.4bytef.magneticFluxDensity", "DPST-14-46", new DPT4ByteFloat("14.046",
            "#.#", "T")), //
    FOURBYTEFLOAT_MAGNETIC_MOMENT("knx.dpt.4bytef.magneticMoment", "DPST-14-47", new DPT4ByteFloat("14.047", "#.#",
            "Am\u00b2")), //
    FOURBYTEFLOAT_MAGNETIC_POLARIZATION("knx.dpt.4bytef.magneticPolarization", "DPST-14-48", new DPT4ByteFloat(
            "14.048", "#.#", "T")), //
    FOURBYTEFLOAT_MAGNETIZATION("knx.dpt.4bytef.magnetization", "DPST-14-49", new DPT4ByteFloat("14.049", "#.#", "A/m")), //
    FOURBYTEFLOAT_MAGNETOMOTIVE_FORCE("knx.dpt.4bytef.magnetomotiveForce", "DPST-14-50", new DPT4ByteFloat("14.050",
            "#.#", "A")), //
    FOURBYTEFLOAT_MASS("knx.dpt.4bytef.mass", "DPST-14-51", new DPT4ByteFloat("14.051", "#.#", "kg")), //
    FOURBYTEFLOAT_MASS_FLUX("knx.dpt.4bytef.massFlux", "DPST-14-52", new DPT4ByteFloat("14.052", "#.#", "kg/s")), //
    FOURBYTEFLOAT_MOMENTUM("knx.dpt.4bytef.momentum", "DPST-14-53", new DPT4ByteFloat("14.053", "#.#", "N/s")), //
    FOURBYTEFLOAT_PHASE_ANGLE_RAD("knx.dpt.4bytef.radiantPhaseAngle", "DPST-14-54", new DPT4ByteFloat("14.054", "#.#",
            "rad")), //
    FOURBYTEFLOAT_PHASE_ANGLE_DEG("knx.dpt.4bytef.degreesPhaseAngle", "DPST-14-55", new DPT4ByteFloat("14.055", "#.#",
            "\u00b0")), //
    FOURBYTEFLOAT_POWER("knx.dpt.4bytef.power", "DPST-14-56", new DPT4ByteFloat("14.056", "#.#", "W")), //
    FOURBYTEFLOAT_POWER_FACTOR("knx.dpt.4bytef.powerFactor", "DPST-14-57", new DPT4ByteFloat("14.057", "#.#",
            "cos\u03A6")), //
    FOURBYTEFLOAT_PRESSURE("knx.dpt.4bytef.pressure", "DPST-14-58", new DPT4ByteFloat("14.058", "#.#", "Pa")), //
    FOURBYTEFLOAT_REACTANCE("knx.dpt.4bytef.reactance", "DPST-14-59", new DPT4ByteFloat("14.059", "#.#", "\u03A9")), //
    FOURBYTEFLOAT_RESISTANCE("knx.dpt.4bytef.resistance", "DPST-14-60", new DPT4ByteFloat("14.060", "#.#", "\u03A9")), //
    FOURBYTEFLOAT_RESISTIVITY("knx.dpt.4bytef.resistivity", "DPST-14-61", new DPT4ByteFloat("14.061", "#.#", "\u03A9m")), //
    FOURBYTEFLOAT_SELF_INDUCTANCE("knx.dpt.4bytef.selfInductance", "DPST-14-62",
            new DPT4ByteFloat("14.062", "#.#", "H")), //
    FOURBYTEFLOAT_SOLID_ANGLE("knx.dpt.4bytef.solidAngle", "DPST-14-63", new DPT4ByteFloat("14.063", "#.#", "sr")), //
    FOURBYTEFLOAT_SOUND_INTENSITY("knx.dpt.4bytef.soundIntensity", "DPST-14-64", new DPT4ByteFloat("14.064", "#.#",
            "w/m\u00b2")), //
    FOURBYTEFLOAT_SPEED("knx.dpt.4bytef.speed", "DPST-14-65", new DPT4ByteFloat("14.065", "#.#", "m/s")), //
    FOURBYTEFLOAT_STRESS("knx.dpt.4bytef.stress", "DPST-14-66", new DPT4ByteFloat("14.066", "#.#", "N/m\u00b2")), //
    FOURBYTEFLOAT_SURFACE_TENSION("knx.dpt.4bytef.surfaceTension", "DPST-14-67", new DPT4ByteFloat("14.067", "#.#",
            "N/m")), //
    FOURBYTEFLOAT_COMMON_TEMPERATURE("knx.dpt.4bytef.commonTemperature", "DPST-14-68", new DPT4ByteFloat("14.068",
            "#.#", "\u00b0C")), //
    FOURBYTEFLOAT_ABSOLUTE_TEMPERATURE("knx.dpt.4bytef.absoluteTemperature", "DPST-14-69", new DPT4ByteFloat("14.069",
            "#.#", "K")), //
    FOURBYTEFLOAT_TEMPERATURE_DIFFERENCE("knx.dpt.4bytef.temperatureDifference", "DPST-14-70", new DPT4ByteFloat(
            "14.070", "#.#", "K")), //
    FOURBYTEFLOAT_THERMAL_CAPACITY("knx.dpt.4bytef.thermalCapacity", "DPST-14-71", new DPT4ByteFloat("14.071", "#.#",
            "J/K")), //
    FOURBYTEFLOAT_THERMAL_CONDUCTIVITY("knx.dpt.4bytef.thermalConductivity", "DPST-14-72", new DPT4ByteFloat("14.072",
            "#.#", "W/mK")), //
    FOURBYTEFLOAT_THERMOELECTRIC_POWER("knx.dpt.4bytef.thermoelectricPower", "DPST-14-73", new DPT4ByteFloat("14.073",
            "#.#", "V/K")), //
    FOURBYTEFLOAT_TIME("knx.dpt.4bytef.time", "DPST-14-74", new DPT4ByteFloat("14.074", "#.#", "s")), //
    FOURBYTEFLOAT_TORQUE("knx.dpt.4bytef.torque", "DPST-14-75", new DPT4ByteFloat("14.075", "#.#", "Nm")), //
    FOURBYTEFLOAT_VOLUME("knx.dpt.4bytef.volume", "DPST-14-76", new DPT4ByteFloat("14.076", "#.#", "m\u00b3")), //
    FOURBYTEFLOAT_VOLUME_FLUX("knx.dpt.4bytef.volumeFlux", "DPST-14-77",
            new DPT4ByteFloat("14.077", "#.#", "m\u00b3/s")), //
    FOURBYTEFLOAT_WEIGHT("knx.dpt.4bytef.weight", "DPST-14-78", new DPT4ByteFloat("14.078", "#.#", "N")), //
    FOURBYTEFLOAT_WORK("knx.dpt.4bytef.work", "DPST-14-79", new DPT4ByteFloat("14.079", "#.#", "J")), //

    //
    //
    // String (16)
    //
    STRING_ASCII("knx.dpt.string.ascii", "DPST-16-0", new DPTString("16.000")), //
    STRING_8859_1("knx.dpt.string.8859_1", "DPST-16-1", new DPTString("16.001")), //
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
    
	public static DatapointType forMajorTypeId(String typeId, boolean isMajorId) {
		for (DatapointType e : values()) {
			if (getMajorTypeId(e.typeId, false).equals(getMajorTypeId(typeId, isMajorId)))
				return e;
		}
		return UNDEFINED;
	}

	private static String getMajorTypeId(String typeId, boolean isMajorId) {
		String majorTypeId = typeId;

		int lastIdx = typeId.lastIndexOf('-');
		if (!isMajorId && lastIdx >= 0) {
			majorTypeId = typeId.substring(0, lastIdx);
		}
		return majorTypeId;
	}
}
