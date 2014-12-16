package fr.exentop.websocket.packets;

import java.io.UnsupportedEncodingException;

public class ExTextPacket extends ExWebsocketPacket{

	public ExTextPacket(String value) {
		super((byte) 1);
		try {
			super.setPayload(value.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
