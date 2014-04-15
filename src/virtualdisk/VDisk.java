package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import common.Constants.DiskOperationType;
import dblockcache.DBuffer;
import java.util.*;
import common.Constants;

public class VDisk extends VirtualDisk {
    
    private Queue<Request> myRequestQueue;
    
    public VDisk (boolean format) throws FileNotFoundException, IOException {
        super(format);
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
            if (request.getDiskOperationType().equals(Constants.DiskOperationType.READ)) {
                //Do this
            }
            else if (request.getDiskOperationType().equals(Constants.DiskOperationType.WRITE)) {
                //Do that
            }
        }
    }

}
