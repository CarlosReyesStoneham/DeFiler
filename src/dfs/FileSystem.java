package dfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import virtualdisk.VDisk;
import common.Constants;
import common.DFileID;
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
        DFileMap = new ConcurrentHashMap<DFileID, Inode>();
    }

    public FileSystem (String volName, boolean format) {
        this._volName = volName;
        this._format = format;
    }

    public FileSystem (boolean format) {
        this(Constants.vdiskName, format);
    }

    public synchronized void writeInode (Inode inode, boolean create) {
        int id = inode.getFileID().getDFileID();

        int blockPosition = id / Constants.INODE_SIZE;
        int offset = (id % Constants.INODE_SIZE) * Constants.INODE_SIZE;

        DBuffer buffer = myCache.getBlock(blockPosition);

        byte[] dataBuffer = new byte[Constants.INODE_SIZE];

        BigInteger dFidBytes = BigInteger.valueOf(inode.getFileID().getDFileID());
        for (int j = 0; j < dFidBytes.toByteArray().length; j++) {
            dataBuffer[j] = dFidBytes.toByteArray()[j];
        }
        /*Adding block pointers to the inode*/
        List<BigInteger> blockMapData = new CopyOnWriteArrayList<BigInteger>();
        for (int block : inode.getMyBlockMap()) {
            blockMapData.add(BigInteger.valueOf(block));
        }
        byte[] intermediateBuffer = new byte[Constants.INODE_SIZE-4];
        int i = 0;
        for (BigInteger bigInt : blockMapData) {
            for (int j = 0; j < bigInt.toByteArray().length; j++) {
                intermediateBuffer[j+i] = bigInt.toByteArray()[j];
            }
            i+=bigInt.toByteArray().length;
        }
        /*Done adding block pointers to the inode*/

        for (int j = 0; j < intermediateBuffer.length; j++) {
            dataBuffer[j + dFidBytes.toByteArray().length] = intermediateBuffer[j];
        }
        
        
        byte[] emptyBuffer = new byte[Constants.INODE_SIZE];
        for (int k = 0; k < emptyBuffer.length; k++) {
        	emptyBuffer[i] = 0;
        }
        
        if(create) {
            buffer.write(dataBuffer, offset, Constants.INODE_SIZE);
        }
        else {
        	buffer.write(emptyBuffer, offset, Constants.INODE_SIZE);
        }
        
        buffer.startPush();
    }

    @Override
    public void init () {
        
        for (int i = 0; i < 16; i++) {
            DBuffer buffer = myCache.getBlock(i);
            
            for (int j=0; j <Constants.BLOCK_SIZE; j+= Constants.INODE_SIZE) {
                byte[] fullBuffer = new byte[Constants.INODE_SIZE];
                int zero = 0;
                for (byte b : fullBuffer) {
                    zero = zero | b;
                }                
                if (zero != 0) {
                    byte[] idBuffer = new byte[4];
                    List<Integer> blockMap = new ArrayList<Integer>();
                    buffer.read(idBuffer, j, 4);
                    
                    for (int k = 4; k < Constants.INODE_SIZE; k+=4) {
                        byte[] blockPointerBuffer = new byte[4];
                        buffer.read(blockPointerBuffer, j+k, 4);
                        
                        ByteBuffer wrapped = ByteBuffer.wrap(blockPointerBuffer); //Big-Endian
                        int num = wrapped.getInt();
                        
                        blockMap.add(num);
                    }
                    
                    ByteBuffer wrapped = ByteBuffer.wrap(idBuffer); //Big-Endian
                    int num = wrapped.getInt();
                    
                    DFileID dfid = new DFileID(num);
                    Inode inode = new Inode(dfid);
                    inode.setMyBlockMap(blockMap);
                    DFileMap.put(dfid, inode);
                }
            }

        }
        for (Inode i : DFileMap.values()) {
            for (int id : i.getMyBlockMap()) {
                blockManager.removeBlock(id);
            }
        }
    }

    @Override
    public synchronized DFileID createDFile () {

        int fileID = 0;
        Set<DFileID> keys = DFileMap.keySet();
        for (DFileID d : keys) {
            if ((d.getDFileID()) == (fileID)) {
                fileID++;
                break;
            }
            else {
                fileID++;
            }
        }

        DFileID dfid = new DFileID(fileID);
        Inode inode = new Inode(dfid);

        writeInode(inode, true);

        DFileMap.put(dfid, inode);
        
        return dfid;
    }

    @Override
    public synchronized void destroyDFile (DFileID dFID) {
        writeInode(DFileMap.get(dFID), false);
        DFileMap.remove(dFID);
        blockManager.addBlock(dFID);
    }

    @Override
    public synchronized int read (DFileID dFID, byte[] buffer, int startOffset, int count) {
        Inode fileInode = DFileMap.get(dFID);

        if (fileInode == null) { return -1; }
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

        Inode inode = new Inode(dFID);

        int currentBytePosition = 0;
        int blockCount = 0;
        while (blockCount < numBlocks) {
            byte[] blockContent = new byte[Constants.BLOCK_SIZE];
            for (int i = currentBytePosition; i < currentBytePosition
                                                  + Constants.BLOCK_SIZE; i++) {
                if (i >= buffer.length) {
                    break;
                }
                blockContent[i-currentBytePosition] = buffer[i];
            }
            currentBytePosition += Constants.BLOCK_SIZE;

            Block block = blockManager.allocateBlock(blockContent);
            inode.addToBlockMap(block.getID());
            // Write blocks to cache
            myCache.getBlock(block.getID());

            blockCount++;
        }
        DFileMap.put(dFID, inode);

        return 0;
    }

    @Override
    public int sizeDFile (DFileID dFID) {
        int size = 0;
        for (Integer i : DFileMap.get(dFID).getMyBlockMap()) {
            size+=i*Constants.BLOCK_SIZE;
        }
        
        return size;
    }

    @Override
    public List<DFileID> listAllDFiles () {
        List<DFileID> files = new ArrayList<DFileID>();
        for (DFileID id : DFileMap.keySet()) {
            files.add(id);
        }
        return files;
    }

    @Override
    public void sync () {
        myCache.sync();

    }

}
