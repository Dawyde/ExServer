package fr.exentop.exserver;

import java.io.IOException;
import java.util.HashMap;

import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.exceptions.ExInvalidMultipartForm;
import fr.exentop.exserver.utils.CircularByteBuffer;

/**
 * Classe permettant la lecture des données post en envoie Multipart. La lecture
 * se fait en stream : champ par champ
 */

public class ExMultipartForm {
	private ExClient mClient;

	private long mRead = 0;
	private long mTotal = 0;
	private String mBoundary = "";
	private byte[] mBoundarySequence;

	private Field mLastField = null;

	public class Field {
		private String mName = null;
		private HashMap<String, String> mDatas = new HashMap<String, String>();

		private boolean mComplete = false;
		private int mRemaining = 0;

		private CircularByteBuffer mBuffer = null;

		public String readValue() throws ExConnectionClosed, IOException {
			// On ne peut pas faire stream + buffer
			if (mBuffer != null || mComplete) return null;
			StringBuffer sb = new StringBuffer();
			while (true) {
				String line = mClient.readLine();
				mRead += line.length() + 2;
				if (isBoundary(line)) break;
				else {
					if (sb.length() != 0) sb.append("\r\n");
					sb.append(line);
				}
			}
			mComplete = true;
			return sb.toString();
		}

		public int read(byte[] tab) throws IOException {
			if (mComplete && mRemaining == 0) return -1;

			// Buffer à remplir
			int to_read = tab.length;
			// Index actuel
			int index = 0;

			// On lit les données
			byte[] buf = new byte[1];
			int len = 0;
			while (to_read > 0 && (len = get(buf)) > 0) {
				tab[index] = buf[0];
				index++;
				to_read--;
			}
			if (len == -1) {
				mComplete = true;
			}
			return index;
		}

		private int get(byte[] buf) throws IOException {
			if (mComplete) {
				if (mRemaining > 0) {
					mRemaining--;
					buf[0] = mBuffer.get();
					return 1;
				}
				return -1;
			}
			if (mBuffer == null) mBuffer = new CircularByteBuffer(mBoundary.length() + 50);
			if (mBuffer.remaining() > 0) {
				buf[0] = mBuffer.get();
				return 1;
			}
			int len;
			int index = 0;
			byte b[] = new byte[1];
			mRemaining = 0;
			do {
				len = mClient.getInputStream().read(b);
				mRead++;
				if (len <= 0) return -1;
				// On est dans la séquence de fin de fichier
				if (index >= mBoundarySequence.length) {

					if (mBoundarySequence.length == index && (b[0] == '-' || b[0] == '\r')) {
						index++;
						continue;
					}
					else if (mBoundarySequence.length + 1 == index && (b[0] == '-' || b[0] == '\n')) {
						if (b[0] == '\n') {
							// On est arrivés à la fin du champ
							mComplete = true;
							if (mRemaining > 0) {
								mRemaining--;
								buf[0] = mBuffer.get(0);
								return 1;
							}
							return -1;
						}
						index++;
						continue;
					}
					else if (mBoundarySequence.length + 2 == index && b[0] == '\r') {
						index++;
						continue;
					}
					else if (mBoundarySequence.length + 3 == index && b[0] == '\n') {
						// On est arrivés à la fin du champ
						mComplete = true;
						if (mRemaining > 0) {
							mRemaining--;
							buf[0] = mBuffer.get(0);
							return 1;
						}
						return -1;
					}
				}
				else if (mBoundarySequence[index] == b[0]) {
					// On place le byte
					mBuffer.put(b[0]);
					index++;
					continue;
				}
				else if (index > 0) {
					mBuffer.put(b[0]);
					int new_index = mBuffer.newStartIndex(mBoundarySequence);
					mRemaining += index - new_index + 1;
					index = new_index;
					if (index == 0) {
						mRemaining--;
						buf[0] = mBuffer.get();
					}

				}
				else buf[0] = b[0];
			} while (index > 0);
			return 1;
		}

		public String getName() {
			return mName;
		}

		public String get(String key) {
			return mDatas.get(key);
		}

		public boolean isFile() {
			return mDatas.containsKey("filename");
		}
	}

	public ExMultipartForm(ExClient client) throws ExConnectionClosed, IOException, ExInvalidMultipartForm {
		mClient = client;
		String data = client.getHeaders().get("content-type");
		if (data.split(";")[0].trim().equalsIgnoreCase("multipart/form-data")) {
			mTotal = Long.parseLong(client.getHeaders().get("content-length"));
			String[] fields = data.split(";");
			for (String field : fields) {
				if (!field.contains("=")) continue;
				String[] datas = field.split("=", 2);
				if (datas[0].trim().equalsIgnoreCase("boundary")) mBoundary = datas[1].trim();
			}
		}
		else throw new ExInvalidMultipartForm();
		String line = mClient.readLine();
		mRead += line.length() + 2;
		if (!isBoundary(line)) throw new ExInvalidMultipartForm();
		mBoundarySequence = ("\r\n--" + mBoundary).getBytes();

	}

	private boolean isBoundary(String line) {
		if (line.length() == mBoundary.length() + 2) {
			if (line.charAt(0) == '-' && line.charAt(1) == '-' && line.substring(2).equals(mBoundary)) return true;
		}
		else if (line.length() == mBoundary.length() + 4) {
			if (line.charAt(0) == '-' && line.charAt(1) == '-' && line.substring(2, line.length() - 2).equals(mBoundary)) return true;
		}
		return false;
	}

	public Field nextField() throws ExConnectionClosed, IOException, ExInvalidMultipartForm {
		if (mRead >= mTotal || (mLastField != null && mLastField.mComplete == false)) return null;
		String line = mClient.readLine();
		mRead += line.length() + 2;
		Field field = new Field();
		while (!line.isEmpty()) {
			String[] datas = line.split(":", 2);
			if (datas[0].trim().equalsIgnoreCase("Content-Disposition")) {
				String[] dispositions = datas[1].split(";");
				if (!dispositions[0].trim().equalsIgnoreCase("form-data")) throw new ExInvalidMultipartForm();
				for (String disposition : dispositions) {
					if (!disposition.contains("=")) continue;
					String[] fields = disposition.split("=", 2);
					if (fields[0].trim().equalsIgnoreCase("name")) {
						String name = fields[1].trim();
						if (name.charAt(0) == '"') name = name.substring(1, name.length() - 1);
						field.mName = name;
					}
					else {
						field.mDatas.put(fields[0].trim(), fields[1].trim());
					}
				}
			}
			else field.mDatas.put(datas[0].trim(), datas[1].trim());
			line = mClient.readLine();
			mRead += line.length() + 2;
		}
		mLastField = field;
		return field;
	}
}
