package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import common.Constants.DiskOperationType;
import dblockcache.DBuffer;
import java.util.*;
import common.Constants;

public class VDisk extends VirtualDisk implements Runnable {

	private Queue<Request> myRequestQueue;

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
