package fr.exentop.exserver;

import java.util.HashMap;

public class ExBag extends HashMap<String, String> {
	
	public boolean has(String key){
		return super.containsKey(key);
	}
}
