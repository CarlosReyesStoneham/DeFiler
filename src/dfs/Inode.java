package dfs;

import common.*;
import java.util.*;

public class Inode {
    private DFileID myID;
    private List<Integer> myBlockMap;

    public Inode (DFileID id) {
        this.myID = id;
        myBlockMap = new ArrayList<Integer>();
    }
    
    public void addToBlockMap(int blockID) {
    	myBlockMap.add(blockID);
    }
    
    public List<Integer> getMyBlockMap () {
        return myBlockMap;
    }
    
    public DFileID getFileID() {
        return this.myID;
    }
    
    public void setMyBlockMap (List<Integer> myBlockMap) {
        this.myBlockMap = myBlockMap;
    }
}