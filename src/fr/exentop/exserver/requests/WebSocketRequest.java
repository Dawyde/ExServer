package fr.exentop.exserver.requests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import fr.exentop.exserver.ExBag;
import fr.exentop.exserver.ExClient;
import fr.exentop.exserver.ExHandshakeEncoder;
import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.exceptions.ExInvalidWebsocketException;
import fr.exentop.exserver.exceptions.ExServerException;
import fr.exentop.exserver.requesthandlers.ExRequestHandler;
import fr.exentop.websocket.packets.ExWebsocketListener;
import fr.exentop.websocket.packets.ExWebsocketPacket;

public class WebSocketRequest extends ExRequest {
	
	private boolean mAccepted = false;
	private String mProtocol = "chat";
	private FragmentMessage mFragmentMessage;
	private ExWebsocketListener mListener = null;
	private long mLastPong = -1;

	private static class FragmentMessage{
		private byte mOpcode;
		private ByteArrayOutputStream mBuffer;
		private int mLen;
		public FragmentMessage(byte opcode){
			mOpcode = opcode;
			mLen = 0;
			mBuffer = new ByteArrayOutputStream();
		}
		
		public void add(byte[] buffer, int len) throws IOException{
			if(buffer != null){
				mBuffer.write(buffer);
				mLen += len;
			}
		}
		public int getLength(){
			return mLen;
		}
		public byte[] getBytes(){
			return mBuffer.toByteArray();
		}
		public byte getOpcode(){
			return mOpcode;
		}
	}
	
	public WebSocketRequest(ExRequestHandler handler, ExClient client) throws ExInvalidWebsocketException {
		super(handler, client);
		ExBag headers = client.getHeaders();
		//On va initialiser la connexion WebSocket
		if(!headers.has("connection") || !headers.get("connection").toLowerCase().contains("upgrade")){
			throw new ExInvalidWebsocketException();
		}
		if(!headers.has("upgrade") || !headers.get("upgrade").equalsIgnoreCase("websocket")){
			throw new ExInvalidWebsocketException();
		}
		
		if(!headers.has("sec-websocket-version") || Integer.parseInt(headers.get("sec-websocket-version")) < 13){
			throw new ExInvalidWebsocketException();
		}
		if(!headers.has("sec-websocket-key")){
			throw new ExInvalidWebsocketException();
		}
	}

	public void sendFrame(ExWebsocketPacket packet) throws IOException{
		OutputStream out = mClient.getOutputStream();
		out.write(packet.getPacket());
		out.flush();
	}
	
	public void accept() throws ExConnectionClosed{
		if(mAccepted) return;
		String handshake = ExHandshakeEncoder.encryptHandshake(mClient.getHeaders());
		setCode(HTTP_SWITCHING_PROTOCOLS);
		setHeader("Upgrade", "websocket");
		setHeader("Connection", "Upgrade");
		setHeader("Sec-WebSocket-Accept", handshake);
		setHeader("Sec-WebSocket-Protocol", mProtocol);
		sendHeaders();
		mAccepted = true;
	}
	
	public void listen() throws ExServerException{
		ExWebsocketPacket packet;
		boolean continuer = true;
		//Lecture des packets
		try{
		while(continuer){
			packet = readFrame();
			if(packet == null) continue;
			switch(packet.getOpcode()){
			case 1://Text
				if(mListener != null) mListener.receiveMessage(new String(packet.getPayload(), "UTF-8"));
				break;
			case 8://Fermeture
				sendFrame(new ExWebsocketPacket((byte) 8, packet.getLength(), packet.getPayload()));
				continuer = false;
				break;
			case 9://Ping
				sendFrame(new ExWebsocketPacket((byte) 10, packet.getLength(), packet.getPayload()));
				break;
			case 10://Pong
				mLastPong = System.currentTimeMillis();
				break;
			}
		}
		}
		catch(IOException e){
			throw new ExConnectionClosed();
		}
	}


	long getLastPing() {
		return mLastPong;
	}
	public void sendPing(){
		try {
			sendFrame(new ExWebsocketPacket((byte) 9,2,new byte[]{65,66}));
		} catch (Exception e) {
		}
	}
	public ExWebsocketPacket readFrame() throws ExInvalidWebsocketException, IOException{
		byte[] c = new byte[2];
		mClient.read(c);
		//Octet 1
		boolean fin = (c[0] & 0x80) == 0x80;
		/*boolean rsv1 = (c[0] & 0x40) == 0x40;
		boolean rsv2 = (c[0] & 0x20) == 0x20;
		boolean rsv3 = (c[0] & 0x10) == 0x10;*/
		
		byte opcode = (byte) (c[0] & 0xF);
		
		if(opcode == 0){
			//Si l'opcode == 0 c'est un packet de continuation, il doit y avoir un packet fragmenté en attente
			if(mFragmentMessage == null){
				//Si y'a pas de packet en attente c'est qu'il y a un probleme
				throw new ExInvalidWebsocketException();
				//print("Paquet malformé");
			}
		}
		if(!fin){
			//Si on est sur du packet fragmenté 
			//Si l'opcode est différent de 0 alors il faut vérifier qu'aucun packet fragmenté n'est en attente
			if(opcode != 0 && mFragmentMessage != null){
				throw new ExInvalidWebsocketException();
				//print("Double message fragmenté");
			}
		}
		
		//Octet 2
		boolean mask = (c[1] & 0x80) == 0x80;
		int len = (c[1] & 0x7F);
		//Longueur
		if(len == 126){//On lit les 2 octets suivants en tant que longueur
			c = new byte[2];
			mClient.read(c);
			len = ((c[0] & 0xFF) << 8) | (c[1] & 0xFF);
		}
		else if(len == 127){//On lit les 4 octets suivants en tant que longueur
			c = new byte[4];
			mClient.read(c);
			len = ((c[0] & 0xFF) << 24) | ((c[1] & 0xFF) << 16) | ((c[2] & 0xFF) << 8) | (c[3] & 0xFF);
		}
		//Masque
		byte[] bitmask = new byte[4];
		if(mask){
			mClient.read(bitmask);
		}
		byte[] buffer_datas = null;
		if(len > 0){
			ByteArrayOutputStream payload = new ByteArrayOutputStream((int) len);
			c = new byte[1];
			int read = 0;
			while(mClient.getInputStream().read(c) > 0){
				if(mask) payload.write(c[0]^bitmask[read%4]);
				else payload.write(c[0]);
				read++;
				if(read >= len) break;
			}
			buffer_datas = payload.toByteArray();
		}
		
		if(!fin){
			if(mFragmentMessage == null){
				mFragmentMessage = new FragmentMessage(opcode);
			}
			mFragmentMessage.add(buffer_datas, len);
			return null;
		}
		else if(opcode == 0){
			mFragmentMessage.add(buffer_datas, len);
			ExWebsocketPacket p = new ExWebsocketPacket(mFragmentMessage.getOpcode(), mFragmentMessage.getLength(), mFragmentMessage.getBytes());
			mFragmentMessage = null;
			return p;
		}
		else if(len > 0){
			return new ExWebsocketPacket(opcode, len, buffer_datas);
		}
		else return new ExWebsocketPacket(opcode);
	}
	
	public void setPacketListener(ExWebsocketListener listener){
		mListener = listener;
	}
}
