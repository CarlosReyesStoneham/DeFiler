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

public class FileSystem extends DFS {

	BufferCache myCache;
	BlockManager myFreeList;
	BlockManager myAllocatedList;
	Map<DFileID, Inode> DFileMap;

	public FileSystem() {
		try {
			myCache = new BufferCache(Constants.BLOCK_SIZE, new VDisk());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		myFreeList = new BlockManager();
		myAllocatedList = new BlockManager();
		DFileMap = new HashMap<DFileID, Inode>();
	}

	@Override
	public void init() {
		//go through blocks in disk and get them using buffers and create
		//dfiles
		for(int i = 0; i < Constants.NUM_OF_BLOCKS-1; i++) {
			Buffer buf = (Buffer) myCache.getBlock(i+1);
			//wait til its valid. NEED WHILE LOOP? 
			buf.startFetch();
			buf.waitValid();
			//create file
			//DFileMap.put(, )
		}
	}

	@Override
	public DFileID createDFile() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		// TODO Auto-generated method stub

	}

	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<DFileID> listAllDFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sync() {
		myCache.sync();

	}

}
