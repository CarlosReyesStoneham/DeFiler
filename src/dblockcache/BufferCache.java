package dblockcache;

import java.util.LinkedList;
import java.util.Queue;

import virtualdisk.VDisk;

import common.Constants;

public class BufferCache extends DBufferCache{

	private Queue<Buffer> blockList;
	private VDisk virtualDisk;

	public BufferCache(int cacheSize, VDisk vd) {
		super(cacheSize);
		blockList = new LinkedList<Buffer>();
		virtualDisk = vd;
	}

	@Override
	public synchronized DBuffer getBlock(int blockID) {
		for(Buffer b : blockList) {
			if(b.getBlockID() == blockID) {
				moveBlockToEnd(b);
				b.setBusy(true);
				return b;
			}
		}

		Buffer buf = new Buffer(blockID, Constants.BLOCK_SIZE, virtualDisk);
		buf.setBusy(true);
		if(!(blockList.size() >= _cacheSize)) {
			blockList.add(buf);
		}
		else{
			evictBlock();
			blockList.add(buf);
		}
		return buf;
	}

	@Override
	public void releaseBlock(DBuffer buf) {
		Buffer b = (Buffer)(buf);
		b.setBusy(false);
		b.notifyAll();

	}

	@Override
	public synchronized void sync() {
		for(Buffer b : blockList) {
			if(!b.checkClean()) {
				b.startPush();
			}
		}
	}

	private void evictBlock() {
		//in case all the blocks are busy
		while(true) {
			for(Buffer b : blockList) {
				if(!b.isBusy()) {
					blockList.remove(b);
					return;
				}
			}
		}
	}

	private void moveBlockToEnd(Buffer b) {
		blockList.remove(b);
		blockList.add(b);
	}

}
