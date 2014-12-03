package fr.exentop.exserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("coucou");
		ExServer server = new ExServer(81);
		server.start();
		try {
			server.getThread().join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
