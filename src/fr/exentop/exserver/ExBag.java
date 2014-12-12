package fr.exentop.exserver;

import java.util.HashMap;

public class ExBag extends HashMap<String, String> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3768167146691928420L;

	public boolean has(String key){
		return super.containsKey(key);
	}
}
