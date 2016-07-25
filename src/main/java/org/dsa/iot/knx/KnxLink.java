package org.dsa.iot.knx;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.serializer.Deserializer;
import org.dsa.iot.dslink.serializer.Serializer;

public class KnxLink {
	Node node;
	Serializer copySerializer;
	Deserializer copyDeserializer;
	
	private KnxLink(Node node, Serializer ser, Deserializer deser) {
		this.node = node;
		this.copySerializer = ser;
		this.copyDeserializer = deser;
	}
	public static void start(Node parent, Serializer copyser, Deserializer copydeser) {
		Node node = parent;
		final KnxLink link = new KnxLink(node, copyser, copydeser);
		link.init();
	}
	
	public void init(){
		
	}
}
