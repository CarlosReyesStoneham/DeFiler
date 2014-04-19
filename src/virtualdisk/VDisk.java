package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import common.Constants.DiskOperationType;
import dblockcache.DBuffer;
import java.util.*;
import common.Constants;

public class VDisk extends VirtualDisk implements Runnable {

	private Queue<Request> myRequestQueue;

	public VDisk(String volName, boolean format)
			throws FileNotFoundException, IOException {

		_volName = volName;
		_maxVolSize = Constants.BLOCK_SIZE * Constants.NUM_OF_BLOCKS;

		/*
		 * mode: rws => Open for reading and writing, as with "rw", and also
		 * require that every update to the file's content or metadata be
		 * written synchronously to the underlying storage device.
		 */
		_file = new RandomAccessFile(_volName, "rws");

		/*
		 * Set the length of the file to be NUM_OF_BLOCKS with each block of
		 * size BLOCK_SIZE. setLength internally invokes ftruncate(2) syscall to
		 * set the length.
		 */
		_file.setLength(Constants.BLOCK_SIZE * Constants.NUM_OF_BLOCKS);
		if (format) {
			formatStore();
		}
		/* Other methods as required */
	}

	public VDisk (boolean format) throws FileNotFoundException, IOException {
		super(format);
		myRequestQueue = new LinkedList<Request>();
	}
	
	public VDisk() throws FileNotFoundException, IOException {
		super();
		myRequestQueue = new LinkedList<Request>();
	}

	@Override
	public synchronized void startRequest (DBuffer buf, DiskOperationType operation)
			throws IllegalArgumentException,
			IOException {
		Request request = new Request(buf, operation);
		myRequestQueue.add(request);
		myRequestQueue.notifyAll();

	}

	public synchronized void handleRequests() {
		while (!myRequestQueue.isEmpty()) {
			Request request = myRequestQueue.poll();
			if(request != null) {
				if (request.getDiskOperationType().equals(Constants.DiskOperationType.READ)) {
					try {
						this.readBlock(request.getBuffer());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else if (request.getDiskOperationType().equals(Constants.DiskOperationType.WRITE)) {
					try {
						this.writeBlock(request.getBuffer());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public synchronized void run() {
		while(true) {
			while(myRequestQueue.isEmpty()) {
				try {
					myRequestQueue.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			handleRequests();
		}
	}
}
