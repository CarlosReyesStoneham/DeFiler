package dfs;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import common.*;

public class BlockManager {

	private Queue<Block> myBlockList;
	class Block {
		private int myID;
		private int mySize;
		private byte[] myContent;
		Block (int id, byte[] content) {
			myID = id;
			myContent = content;
			mySize = Constants.BLOCK_SIZE;
		}

		public int getID() {
			return myID;
		}
	}


	public BlockManager() {
		myBlockList = new ConcurrentLinkedQueue<Block>();
		for (int i = 0; i < Constants.NUM_OF_BLOCKS; i++) {
			myBlockList.add(new Block(i, null));
		}
	}

	public synchronized Block allocateBlock(byte[] content) {
		Block block = myBlockList.poll();
		block.myContent = content;
		return block;
	}

	public synchronized Queue<Block> getBlockList() {
		return this.myBlockList;
	}

	public synchronized void removeBlock(int index) {
		for (Block b : myBlockList) {
			if (b.getID() == index) {
				myBlockList.remove(b);
			}
		}
	}
	public synchronized void addBlock(DFileID dFID) {
		myBlockList.add(new Block(dFID.getDFileID(), null));

	}



}

