package fr.exentop.exserver.utils;

public class CircularByteBuffer {
	private byte[] mBuffer;
	private int mReadIndex = 0;
	private int mWriteIndex = 0;
	private int mRemaining = 0;

	public CircularByteBuffer(int len) {
		mBuffer = new byte[len];
	}

	public void put(byte b) {
		mBuffer[mWriteIndex] = b;
		mWriteIndex = (mWriteIndex + 1) % mBuffer.length;
		mRemaining++;
	}

	public byte get() {
		byte r = mBuffer[mReadIndex];
		mReadIndex = (mReadIndex + 1) % mBuffer.length;
		mRemaining--;
		return r;
	}

	public int remaining() {
		return mRemaining;
	}

	public byte get(int i) {
		return mBuffer[(mReadIndex + i) % mBuffer.length];
	}

	public void remove(int nb) {
		mRemaining -= nb;
	}

	public int newStartIndex(byte[] sequence) {
		int start = 1;
		while (start < mRemaining) {
			boolean valid = true;
			for (int i = 0; i < mRemaining - start; i++) {
				if (sequence[i] != get(start + i)) {
					valid = false;
					break;
				}
			}
			if (valid) break;
			start++;
		}
		return mRemaining - start;
	}
}
