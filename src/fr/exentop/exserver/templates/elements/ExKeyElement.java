package fr.exentop.exserver.templates.elements;

import fr.exentop.exserver.templates.ExTemplate;

public class ExKeyElement implements ExElement {
	
	private String mKey;
	
	public ExKeyElement(String key){
		mKey = key;
	}

	@Override
	public String render(ExTemplate template) {
		return template.get(mKey);
	}

}
