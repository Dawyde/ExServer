package fr.exentop.exserver.templates;

import java.util.TreeMap;

public class ExTemplate {
	private String mPath;
	private TreeMap<String, String> mDatas = null;
	
	public ExTemplate(String path){
		mPath = path;
	}
	
	public void set(String key, String value){
		if(mDatas == null) mDatas = new TreeMap<String, String>();
		mDatas.put(key, value);
	}
	public String get(String key){
		if(mDatas == null) return null;
		return mDatas.get(key);
	}
	public String getPath(){
		return mPath;
	}
}
