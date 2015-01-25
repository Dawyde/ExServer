package fr.exentop.exserver.templates;

import java.io.File;
import java.util.TreeMap;

import fr.exentop.exserver.ExServer;

public class ExTemplates {
	private TreeMap<String, ExTemplateInstance> mTemplates;
	private String mDirectory;
	private boolean mUseCache;

	public ExTemplates(String directory) {
		mTemplates = new TreeMap<String, ExTemplateInstance>();
		mDirectory = directory;
		mUseCache = ExServer.USE_TEMPLATE_CACHE;
	}

	public ExTemplateInstance getInstance(String path) {
		ExTemplateInstance instance = mTemplates.get(path);
		if (instance == null) {
			// Sinon on tente de le charger
			File file = new File(mDirectory + "/" + path);
			if (!file.exists()) return null;
			instance = new ExTemplateInstance(this, file);
			if (mUseCache) mTemplates.put(path, instance);
		}
		return instance;
	}

	public void clear() {
		mTemplates.clear();
	}

	public String render(ExTemplate template) {
		// On regarde si le template est déjà préchargé
		ExTemplateInstance instance = getInstance(template.getPath());

		return instance.render(template);
	}
}
