package fr.exentop.exserver.requests;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;

import fr.exentop.exserver.ExClient;
import fr.exentop.exserver.ExServer;
import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.requesthandlers.ExRequestHandler;

public abstract class ExRequest {

	public static final int HTTP_SWITCHING_PROTOCOLS = 101;
	public static final int HTTP_OK = 200;
	public static final int HTTP_PARTIAL_CONTENT = 206;
	public static final int HTTP_NOT_MODIFIED = 304;
	public static final int HTTP_TEMPORARY_REDIRECTED = 307;
	public static final int HTTP_NOT_FOUND = 404;
	public static final int HTTP_INTERNAL_SERVER_ERROR = 500;

	public static final int BUFFER_LEN = 1024;
	public final static SimpleDateFormat sDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);

	protected ExRequestHandler mHandler;

	protected ExClient mClient;
	protected HashMap<String, String> mHeaders;
	protected int mCode = 200;

	protected boolean mNoCache = false;

	protected HashSet<String> mSetCookies = null;

	private boolean mHeadersSent = false;

	public ExRequest(ExRequestHandler handler, ExClient client) {
		mHandler = handler;
		mClient = client;
		mHeaders = new HashMap<String, String>();

		// On ajouter les Headers par défaut
		mHeaders.put("Date", sDateFormat.format(new Date()));
		mHeaders.put("Server", ExServer.SERVER_NAME);

	}

	public ExClient getClient() {
		return mClient;
	}

	public ExRequestHandler getRequestHandler() {
		return mHandler;
	}

	public void setCode(int code) {
		mCode = code;
	}

	public boolean isSSL() {
		return mClient.isSSL();
	}

	public void noCache() {
		mNoCache = true;
	}

	protected String getMessage(int type) {
		switch (type) {
			case HTTP_OK:
				return "OK";
			case HTTP_NOT_FOUND:
				return "Not Found";
			case HTTP_NOT_MODIFIED:
				return "Not Modified";
			case HTTP_SWITCHING_PROTOCOLS:
				return "Switching Protocols";
			case HTTP_PARTIAL_CONTENT:
				return "Partial Content";
			case HTTP_TEMPORARY_REDIRECTED:
				return "Temporary Retirected";
			case HTTP_INTERNAL_SERVER_ERROR:
				return "Internal Server Error";
		}
		return "OK";
	}

	public String getURI() {
		return mClient.getURI();
	}

	public void sendHeaders() throws ExConnectionClosed {
		if (mHeadersSent) return;
		mHeadersSent = true;
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 ").append(mCode).append(" ").append(getMessage(mCode)).append("\r\n");
		for (Entry<String, String> entry : mHeaders.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
		}
		if (mSetCookies != null) {
			for (String cookies : mSetCookies) {
				sb.append("Set-Cookie: ").append(cookies).append("\r\n");
			}
		}
		sb.append("\r\n");

		// On envoie les headers
		OutputStream output = mClient.getOutputStream();
		try {
			output.write(sb.toString().getBytes());
			output.flush();
		}
		catch (Exception e) {
			throw new ExConnectionClosed();
		}
	}

	public void setHeader(String key, String value) {
		mHeaders.put(key, value);
	}

	public void redirect(String url) throws ExConnectionClosed {
		setCode(HTTP_TEMPORARY_REDIRECTED);
		mHeaders.put("Location", url);
		mHeaders.put("Content-Length", "0");
		sendHeaders();
	}

	public void sendTextResponse(String text) throws ExConnectionClosed {
		sendTextResponse(text, false);
	}

	public void setCookie(String name, String value) {
		if (mSetCookies == null) mSetCookies = new HashSet<String>();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 1);
		try {
			mSetCookies.add(name + "=" + URLEncoder.encode(value, "UTF-8") + "; expires=" + sDateFormat.format(c.getTime()));
		}
		catch (UnsupportedEncodingException e) {
		}
	}

	public void sendTextResponse(String text, boolean utf8) throws ExConnectionClosed {
		byte[] datas;
		if (utf8) {
			try {
				datas = text.getBytes("UTF-8");
			}
			catch (UnsupportedEncodingException e1) {
				datas = text.getBytes();
			}
		}
		else {
			datas = text.getBytes();
		}
		mHeaders.put("Content-Length", String.valueOf(datas.length));
		if (!mNoCache) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, 1);
			mHeaders.put("Expires", sDateFormat.format(c.getTime()));
		}
		if (!mHeaders.containsKey("Content-Type")) mHeaders.put("Content-Type", "text/html");
		// On envoie le headers
		sendHeaders();

		OutputStream output = mClient.getOutputStream();
		int len = datas.length;
		int offset = 0;
		try {
			while (len > 0) {
				output.write(datas, offset, len > BUFFER_LEN ? BUFFER_LEN : len);
				offset += BUFFER_LEN;
				len -= BUFFER_LEN;
				output.flush();
			}
		}
		catch (Exception e) {
			throw new ExConnectionClosed();
		}
	}
}
