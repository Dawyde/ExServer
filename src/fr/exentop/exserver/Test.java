package fr.exentop.exserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExRouter r = new ExRouter();
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/test");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		r.addRoute("/t/*est");
		/*r.addRoute("/tes/t");
		r.addRoute("/*t*e/* *st");*/
		r.addRoute("/t/*/e*t");
		
		long r2 = System.currentTimeMillis();
		r.match("/t/de/est");
		System.out.println(System.currentTimeMillis()-r2);
		
		/*
		System.out.println("coucou");
		ExServer server = new ExServer(81);
		server.start();
		try {
			server.getThread().join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
