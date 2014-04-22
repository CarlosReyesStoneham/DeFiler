package dfs;

import common.*;
import java.util.*;

public class Inode {
    private DFileID myID;
    private int myFileSize;
    private List<Integer> myBlockMap;

    public Inode (DFileID id, int fileSize) {
        this.myID = id;
        this.myFileSize = fileSize;
        myBlockMap = new ArrayList<Integer>();
    }
    
    public void addToBlockMap(int blockID) {
    	myBlockMap.add(blockID);
    }
    
    public List<Integer> getMyBlockMap () {
        return myBlockMap;
    }
    
    public int getFileSize() {
        return this.myFileSize;
    }
    
    public DFileID getFileID() {
        return this.myID;
    }
    
    public void setMyBlockMap (List<Integer> myBlockMap) {
        this.myBlockMap = myBlockMap;
    }
}