package fr.exentop.exserver.requests;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Date;

import fr.exentop.exserver.ExClient;
import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.requesthandlers.ExRequestHandler;

public class HTTPRequest extends ExRequest {

	private final static int FILE_BUFFER = 4096;

	public HTTPRequest(ExRequestHandler handler, ExClient client) {
		super(handler, client);
	}

	protected String getMimeType(File f) {
		String[] s = f.getName().split("\\.");
		if (s.length <= 1) return "octet/stream";
		String ext = s[s.length - 1].toLowerCase();
		if (ext.equals("html")) return "text/html";
		else if (ext.equals("js")) return "application/javascript";
		else if (ext.equals("jpg")) return "image/jpeg";
		else if (ext.equals("jpeg")) return "image/jpeg";
		else if (ext.equals("png")) return "image/png";
		else if (ext.equals("gif")) return "image/gif";
		else if (ext.equals("mp3")) return "audio/mpeg";
		else if (ext.equals("mp4")) return "video/mp4";
		else if (ext.equals("css")) return "text/css";
		return "octet/stream";
	}

	public void sendFile(File file) throws ExConnectionClosed {
		try {
			Date last_modified = new Date(file.lastModified() / 1000 * 1000);
			// last_modified.
			// On vérifie si on doit vraiment renvoyer l'image
			if (mClient.getHeaders().has("if-modified-since")) {
				try {
					Date d = sDateFormat.parse(mClient.getHeaders().get("if-modified-since"));
					if (!last_modified.after(d)) {
						// Pas de modification
						setCode(HTTP_NOT_MODIFIED);
						sendHeaders();
						return;
					}
				}
				catch (Exception e) {
				}
			}
			long len = file.length();
			long total = len;
			mHeaders.put("Content-Length", String.valueOf(len));
			mHeaders.put("Content-Type", getMimeType(file));
			mHeaders.put("Last-Modified", sDateFormat.format(last_modified));

			// On regarde s'il y a un range
			long start = 0;
			if (mClient.getHeaders().has("range")) {
				String[] datas = mClient.getHeaders().get("range").split("=");
				if (datas.length == 2 && datas[0].equals("bytes")) {
					mHeaders.put("Accept-Range", "bytes");
					setCode(HTTP_PARTIAL_CONTENT);
					try {
						String[] range = datas[1].split("-");
						if (range.length < 2 || datas[1].trim().isEmpty()) {
							long sb = Long.parseLong(range[0]);
							if (sb <= len) {
								start = sb;
								len = total - sb;
								mHeaders.put("Content-Length", String.valueOf(len));
							}
						}
						else if (range.length == 2) {
							long sb = Long.parseLong(range[0]);
							long eb = Long.parseLong(range[1]);
							if (sb <= eb && eb <= len) {
								start = sb;
								len = eb - sb;
								mHeaders.put("Content-Length", String.valueOf(len));
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					mHeaders.put("Content-Range", "bytes " + start + "-" + (start + len - 1) + "/" + total);
				}
			}
			// On envoie le headers
			sendHeaders();

			OutputStream output = mClient.getOutputStream();
			FileInputStream input = new FileInputStream(file);

			if (start > 0) input.skip(start);

			byte[] buffer = new byte[FILE_BUFFER];
			int read = 0;
			while (len > 0) {
				read = input.read(buffer);
				if (read > len) read = (int) len;
				output.write(buffer, 0, read);
				output.flush();
				len -= read;
			}
			input.close();
		}
		catch (Exception e) {
			throw new ExConnectionClosed();
		}

	}

}
