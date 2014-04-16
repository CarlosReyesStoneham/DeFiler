package dblockcache;

import java.io.IOException;

import common.Constants.DiskOperationType;

import virtualdisk.VDisk;
import virtualdisk.VirtualDisk;

public class Buffer extends DBuffer {
	private int blockID;
	private byte[] block;
	private boolean isValid;
	private boolean isBusy;
	private boolean isDirty;
	private VDisk virtualDisk;
	
	public Buffer (int id, int blockSize, VDisk vDisk) {
		blockID = id;
		block = new byte[blockSize];
		virtualDisk = vDisk;
	}
	
    @Override
    public void startFetch () {
        isBusy = true;
        isValid = false;
        try {
			virtualDisk.startRequest(this, DiskOperationType.READ);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }

    @Override
    public void startPush () {
        isBusy = true;
        try {
			virtualDisk.startRequest(this, DiskOperationType.WRITE);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @Override
    public boolean checkValid () {
        return isValid;
    }

    @Override
    public boolean waitValid () {
        while(!isValid) {
        	try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        return true;
    }

    @Override
    public boolean checkClean () {
        return !isDirty;
    }

    @Override
    public boolean waitClean () {
        while(isDirty) {
        	try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        return true;
    }

    @Override
    public boolean isBusy () {
        return isBusy;
    }

    @Override
    public int read (byte[] buffer, int startOffset, int count) {
        isBusy = true;
        int bytes = count;
        if (count > buffer.length){
        	bytes = buffer.length;
        }
        //check if buffer is valid
        if (startOffset + bytes > buffer.length || startOffset + bytes < 0 || !isValid) {
        	return -1;
        }
        
        //if at end of file
        for (int i=startOffset; i < bytes; i++) {
        	//check if this works
        	if(i == (bytes-1)) {
        		notifyAll();
        		return bytes;
        	}
        	else {
        		buffer[i] = block[i - startOffset]; 
        	}
        }
        isBusy = false;
        notifyAll();
        return bytes;
    }

    @Override
    public int write (byte[] buffer, int startOffset, int count) {
        // TODO Auto-generated method stub
        isBusy = true;
        int bytes = count;
        if (count > buffer.length) {
        	bytes = buffer.length;
        }
        if (startOffset + bytes > buffer.length || startOffset + bytes < 0 || !isValid) {
        	return -1;
        }
    	isDirty = true;
        for (int i = startOffset; i<bytes; i++) {
        	if (i < (bytes-1)) {
        		notifyAll();
        		return bytes;
        		}
        
        	else {
        		block[i - startOffset] = buffer[i]; 
        }
        }
    	isBusy = false;
    	notifyAll();
    	return bytes;
    }

    @Override
    public void ioComplete () {
        isBusy = false;
        isValid = true;
        isDirty = false;
        notifyAll();
    }

    @Override
    public int getBlockID () {
      return blockID;
    }

    @Override
    public byte[] getBuffer () {
        return block;
    }

}
