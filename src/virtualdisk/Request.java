package virtualdisk;

import common.Constants.DiskOperationType;
import dblockcache.DBuffer;

public class Request {
    private DBuffer myBuf;
    private DiskOperationType myDiskOperation;
    
    public Request (DBuffer buf, DiskOperationType op) {
        this.myBuf = buf;
        this.myDiskOperation = op;
    }
    
    public DBuffer getBuffer() {
        return this.myBuf;
    }
    
    public DiskOperationType getDiskOperationType() {
        return this.myDiskOperation;
    }
}
