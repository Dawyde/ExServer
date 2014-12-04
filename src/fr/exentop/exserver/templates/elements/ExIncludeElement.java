package fr.exentop.exserver.templates.elements;

import fr.exentop.exserver.templates.ExTemplate;
import fr.exentop.exserver.templates.ExTemplateInstance;

public class ExIncludeElement implements ExElement {

	private ExTemplateInstance mInstance; 
	public ExIncludeElement(ExTemplateInstance template){
		mInstance = template;
	}
	
	@Override
	public String render(ExTemplate template) {
		if(mInstance == null) return "null";
		return mInstance.render(template);
	}

}
