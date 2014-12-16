package fr.exentop.websocket.packets;

public interface ExWebsocketListener {
	public void receiveMessage(String message);
	public void connexionClosed();
}
