package fr.exentop.exserver.templates;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import fr.exentop.exserver.templates.elements.ExElement;
import fr.exentop.exserver.templates.elements.ExIncludeElement;
import fr.exentop.exserver.templates.elements.ExKeyElement;

public class ExTemplateInstance {
	private String[] mDatas;
	private ExElement[] mKeys;
	private ExTemplates mTemplates;
	
	public ExTemplateInstance(ExTemplates templates, File file){
		mTemplates = templates;
		ArrayList<String> datas = new ArrayList<String>();
		ArrayList<ExElement> keys = new ArrayList<ExElement>();
		try {
			
			FileInputStream input = new FileInputStream(file);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			boolean in_key = false;
			byte[] b = new byte[1];
			int nb = 0;
			while(input.read(b) > 0){
				if(b[0] == '{' && in_key == false){
					nb++;
					if(nb == 2){
						datas.add(new String(buffer.toByteArray(), "UTF-8"));
						buffer.reset();
						in_key = true;
						nb = 0;
					}
				}
				else if(b[0] == '}' && in_key == true){
					nb++;
					if(nb == 2){
						ExElement element = null;
						String key = new String(buffer.toByteArray(), "UTF-8").trim();
						if(key.startsWith("include=")){
							element = new ExIncludeElement(mTemplates.getInstance(key.substring(8)));
						}
						else element = new ExKeyElement(key);
						keys.add(element);
						buffer.reset();
						in_key = false;
						nb = 0;
					}
				}
				else{
					if(nb == 1){
						buffer.write((int) (in_key?'}':'{'));
					}
					nb=0;
					buffer.write(b);
				}
			}
			if(!in_key){
				datas.add(new String(buffer.toByteArray(), "UTF-8"));
			}
			mDatas = new String[datas.size()];
			for(int i=0;i<mDatas.length;i++) mDatas[i] = datas.get(i);
			mKeys = new ExElement[keys.size()];
			for(int i=0;i<mKeys.length;i++) mKeys[i] = keys.get(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String render(ExTemplate template){
		StringBuffer buffer = new StringBuffer();
		for(int i=0;i<mDatas.length;i++){
			buffer.append(mDatas[i]);
			if(mKeys.length>i){
				buffer.append(mKeys[i].render(template));
			}
		}
		return buffer.toString();
	}
}
