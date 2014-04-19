package dfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import virtualdisk.VDisk;
import common.Constants;
import common.DFileID;
import dblockcache.Buffer;
import dblockcache.BufferCache;
import dblockcache.DBuffer;
import dfs.BlockManager.Block;


public class FileSystem extends DFS {

    BufferCache myCache;
    BlockManager blockManager;
    Map<DFileID, Inode> DFileMap;
    

    public FileSystem () {
        try {
            myCache = new BufferCache(Constants.BLOCK_SIZE, new VDisk());
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        blockManager = new BlockManager();
        DFileMap = new HashMap<DFileID, Inode>();
    }

    @Override
    public void init () {
    
       //go through the list of used inodes and remove the allocated blocks from the freelist
       for (Inode i : DFileMap.values()) {
    	   for (int id : i.getMyBlockMap()) {
    		   blockManager.removeBlock(id);
    	   }
       }
    }

    @Override
    public DFileID createDFile () {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void destroyDFile (DFileID dFID) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized int read (DFileID dFID, byte[] buffer, int startOffset, int count) {
        Inode fileInode = DFileMap.get(dFID);
        
        if (fileInode == null) {
            return -1;
        }
        int currentOffset = startOffset;
        int currentCount = count;
        for (int blockPointer : fileInode.getMyBlockMap()) {
            DBuffer buf = myCache.getBlock(blockPointer);
            buf.read(buffer, currentOffset, currentCount);
            currentOffset += count;
            currentCount -= count;
        }
        
        return count;
        
    }

    /*
     * writes to the file specified by DFileID from the buffer starting from the
     * buffer offset startOffset; at most count bytes are transferred
     */
    @Override
    public synchronized int write (DFileID dFID, byte[] buffer, int startOffset, int count) {
        int fileSize = buffer.length;

        int numBlocks;
        if (fileSize % Constants.BLOCK_SIZE != 0) {
            numBlocks = fileSize / Constants.BLOCK_SIZE + 1;
        }
        else {
            numBlocks = fileSize / Constants.BLOCK_SIZE;
        }

        Inode inode = new Inode(dFID, fileSize);

        int currentBytePosition = 0;
        int blockCount = 0;
        while (blockCount < numBlocks) {
            byte[] blockContent = new byte[Constants.BLOCK_SIZE];
            for (int i = currentBytePosition; i < currentBytePosition
                                                  + Constants.BLOCK_SIZE; i++) {
                if (i > buffer.length) {
                    break;
                }
                blockContent[i] = buffer[i];
            }
            currentBytePosition += Constants.BLOCK_SIZE;

            Block block = blockManager.allocateBlock(blockContent);
            inode.addToBlockMap(block.getID());
            blockCount++;
        }

        // PUT THE INODE SOMEWHERE

        return 0;
    }

    @Override
    public int sizeDFile (DFileID dFID) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<DFileID> listAllDFiles () {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sync () {
        myCache.sync();

    }
   
}
