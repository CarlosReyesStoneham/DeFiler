package dfs;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import common.*;

public class BlockManager {
	
	private Queue<Block> myBlockList;
	private int maxBlockNumber;
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
		maxBlockNumber = 0;
		myBlockList = new LinkedList<Block>();
		for (int i = 0; i < maxBlockNumber; i++) {
			myBlockList.add(new Block(i, null));
		}
	}
	
	public Block allocateBlock(byte[] content) {
		Block block = myBlockList.poll();
		block.myContent = content;
		return block;
	}
	
	public Queue<Block> getBlockList() {
		return this.myBlockList;
	}
	
	public void removeBlock(int index) {
		for (Block b : myBlockList) {
			if (b.getID() == index) {
				myBlockList.remove(b);
			}
		}
	}
	
}
