package dfs;

import common.*;
import java.util.*;

public class Inode {
    DFileID myID;
    int myFileSize;
    List<DFileID> myBlockMap;
    
    public Inode (DFileID id, int fileSize) {
        this.myID = id;
        this.myFileSize = fileSize;
        myBlockMap = new ArrayList<DFileID>();
    }
}