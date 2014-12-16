package fr.exentop.websocket.packets;

public class ExWebsocketPacket {
	private int mLength;
	private byte[] mPayload = null;
	private byte mOpcode;
	
	public ExWebsocketPacket(byte opcode){
		mOpcode = opcode;
		mLength = 0;
	}
	public ExWebsocketPacket(byte opcode, int length, byte[] payload){
		mOpcode = opcode;
		mLength = length;
		mPayload = payload;
	}
	public void setPayload(byte[] bytes){
		mPayload = bytes;
		mLength = bytes.length;
	}
	public int getOpcode(){
		return mOpcode;
	}
	public int getLength(){
		return mLength;
	}
	public byte[] getPayload(){
		return mPayload;
	}
	
	public byte[] getPacket(){
		int len = mLength;
		byte[] hdata;
		if(len > 65536){
			hdata = new byte[6+len];
			hdata[1] = 127;
			hdata[2] = (byte) (len >> 24 & 0xFF);
			hdata[3] = (byte) (len >> 16 & 0xFF);
			hdata[4] = (byte) (len >> 8 & 0xFF);
			hdata[5] = (byte) (len & 0xFF);
			if(len > 0) System.arraycopy(mPayload, 0, hdata, 6, len);
		}
		else if(len > 125){
			hdata = new byte[4+len];
			hdata[1] = 126;
			hdata[2] = (byte) (len >> 8 & 0xFF);
			hdata[3] = (byte) (len & 0xFF);
			if(len > 0) System.arraycopy(mPayload, 0, hdata, 4, len);
		}
		else{
			hdata = new byte[2+len];
			hdata[1] = (byte) len;
			if(len > 0) System.arraycopy(mPayload, 0, hdata, 2, len);
		}
		hdata[0] = (byte) (0x80 | mOpcode);
		return hdata;
	}
}
