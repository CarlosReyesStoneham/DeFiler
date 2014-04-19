package dfs;

import java.util.LinkedList;
import java.util.List;
import common.*;

public class BlockManager {
	
	List<Block> myBlockList;
	
	class Block {
		private int myIndex;
		private int mySize;
		Block (int index) {
			myIndex = index;
			mySize = Constants.BLOCK_SIZE;
		}
		
		public int getIndex() {
			return myIndex;
		}
	}
	
	
	public BlockManager() {
		myBlockList = new LinkedList<Block>();
	}
	
	public void addBlock(int index) {
		Block block = new Block(index);
		myBlockList.add(block);
	}
	
	public void removeBlock(int index) {
		for (Block b : myBlockList) {
			if (b.getIndex() == index) {
				myBlockList.remove(b);
			}
		}
	}
	
}
