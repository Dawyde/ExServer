package fr.exentop.test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import fr.exentop.exserver.ExServer;
import fr.exentop.exserver.actions.ExAction;
import fr.exentop.exserver.actions.ExResourceAction;
import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.requesthandlers.DefaultExRequestHandler;
import fr.exentop.exserver.requests.HTTPRequest;
import fr.exentop.exserver.templates.ExTemplate;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExServer.KEYSTORE_PATH = "mySrvKeystore";
		ExServer.KEYSTORE_PASSWORD = "maclio";
		ExServer server = new ExServer();
		server.setHttpEnable(true);
		server.setHttpsEnable(true);
		server.start();
		DefaultExRequestHandler d = (DefaultExRequestHandler) server.getRequestHandler();
		d.getRouter().addRoute("/images/*", new ExResourceAction("resources/images"));
		d.getRouter().addRoute("/scripts/*", new ExResourceAction("resources/scripts"));
		d.getRouter().addRoute("/", new ExAction() {
			@Override
			public void runAction(HTTPRequest request, String[] parameters) throws ExConnectionClosed {
				DefaultExRequestHandler handler = (DefaultExRequestHandler) request.getRequestHandler();
				ExTemplate t = new ExTemplate("template.tpl");
				t.set("prenom", "John");

				handler.render(t, request);
			}
		});
		d.getRouter().addRoute("/bou", new ExAction() {
			@Override
			public void runAction(HTTPRequest request, String[] parameters) throws ExConnectionClosed {
				request.redirect("/");
			}
		});
		d.getRouter().addRoute("/user/*", new ExAction() {
			@Override
			public void runAction(HTTPRequest request, String[] parameters) throws ExConnectionClosed {
				try {
					request.sendTextResponse("Salut <b>" + URLDecoder.decode(parameters[0], "UTF8") + "</b> "
							+ (request.isSSL() ? "HTTPS" : "HTTP") + " !");
				}
				catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		d.getRouter().addRoute("/clear", new ExAction() {
			@Override
			public void runAction(HTTPRequest request, String[] parameters) throws ExConnectionClosed {
				((DefaultExRequestHandler) request.getRequestHandler()).getExTemplates().clear();
				request.sendTextResponse("Cleared");
			}
		});
		try {
			System.out.println("Test");
			server.getHttpServer().getThread().join();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
