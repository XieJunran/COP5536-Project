import java.util.*;

public class bplustree {
	
	final int MaxKeyNum;
	final int MinKeyNum;
	final int MaxChildrenNum;
	
	Node root;
	
	bplustree(int order){
		this.MaxKeyNum = order - 1;
		this.MinKeyNum = (int)Math.ceil(((double)order)/2) - 1;
		this.MaxChildrenNum = order;
		this.root = new ExternalNode();
	}
	
	public void insert(int key, double value) {
		root.insert(key, value);
	}
	
	public String search(int key) {
		return root.search(key);
	}
	
	public String search(int startKey, int endKey) {
		return root.search(startKey, endKey);
	}
	
	public void delete(int key) {
		root.delete(key);
	}
	
	public void displayWholeTree() {
		Queue<Node> curLevel = new LinkedList<>();
		Queue<Node> nextLevel = new LinkedList<>();
		curLevel.add(root);
		while(curLevel.size() != 0) {
			Node curNode = curLevel.poll();
			curNode.display(nextLevel);
			if(curLevel.size() == 0) {
				Queue<Node> tmp = curLevel;
				curLevel = nextLevel;
				nextLevel = tmp;
				System.out.println();
			}
		}
	}
	
	abstract class Node {
		int keyNum;
		int[] keys;
		InternalNode parent;
		
		abstract public void insert(int key, double value);
		abstract public String search(int key);
		abstract public String search(int startKey, int endKey);
		abstract public void delete(int key);
		abstract public void display(Queue<Node> nextLevel);
	}
	
	class InternalNode extends Node {
		
		Node[] children;
		
		InternalNode(){
			super.keyNum = 0;
			super.keys = new int[MaxKeyNum];
			super.parent = null;
			this.children = new Node[MaxChildrenNum];
			this.parent = null;
		}
		
		public void insert(int key, double value) {
			int index = getIndex(key);
			children[index].insert(key, value);
		}
		
		public void insert(int key, Node child) {
			if(keyNum == MaxKeyNum) {
				insertFull(key, child);
			}else {
				insertNotFull(key, child);
			}
		}
		
		public String search(int key) {
			int index = getIndex(key);
			return children[index].search(key);
		}
		
		public String search(int startKey, int endKey) {
			int index = getIndex(startKey);
			return children[index].search(startKey, endKey);
		}
		
		public void delete(int key) {
			int index = getIndex(key);
			children[index].delete(key);
		}
		
		public void display(Queue<Node> nextLevel) {
			for(int i = 0; i < keyNum; i++) {
				if(i != keyNum - 1)
					System.out.print(keys[i] + ",");
				else
					System.out.print(keys[i]);
			}
			if(parent == null)
				System.out.print("[!!!!!!!]");
			System.out.print("   ");
			for(int i = 0; i <= keyNum; i++) {
				nextLevel.add(children[i]);
			}
		}
		
		public void borrowOrMergeFromSibling() {
			//If root is deficient
			if(parent == null) {
				if(keyNum == 0) {
					root = children[0];
					root.parent = null;
				}
				return;
			}
			int indexOfParent = 0;
			for(int i = 0; i <= parent.keyNum; i++) {
				if(parent.children[i] == this) {
					indexOfParent = i;
					break;
				}
			}
			if(indexOfParent != 0 && parent.children[indexOfParent - 1].keyNum > MinKeyNum) {
				//Borrow from left sibling
				InternalNode sibling = (InternalNode)parent.children[indexOfParent - 1];
				System.arraycopy(keys, 0, keys, 1, keyNum);
				System.arraycopy(children, 0, children, 1, keyNum + 1);
				keys[0] = parent.keys[indexOfParent - 1];
				children[0] = sibling.children[sibling.keyNum];
				children[0].parent = this;
				keyNum++;
				parent.keys[indexOfParent - 1] = sibling.keys[sibling.keyNum - 1];
				sibling.keyNum--;
			}else if(indexOfParent != parent.keyNum && parent.children[indexOfParent + 1].keyNum > MinKeyNum) {
				//Borrow from right sibling
				InternalNode sibling = (InternalNode)parent.children[indexOfParent + 1];
				keys[keyNum] = parent.keys[indexOfParent];
				children[keyNum + 1] = sibling.children[0];
				children[keyNum + 1].parent = this;
				keyNum++;
				parent.keys[indexOfParent] = sibling.keys[0];
				System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyNum - 1);
				System.arraycopy(sibling.children, 1, sibling.children, 0, sibling.keyNum);
				sibling.keyNum--;
			}else {
				//Start merging
				if(indexOfParent == 0) {
					//Merge with right sibling
					InternalNode sibling = (InternalNode)parent.children[indexOfParent + 1];
					keys[keyNum] = parent.keys[0];
					System.arraycopy(sibling.keys, 0, keys, keyNum + 1, sibling.keyNum);
					System.arraycopy(sibling.children, 0, children, keyNum + 1, sibling.keyNum + 1);
					keyNum += 1 + sibling.keyNum;
					for(int i = 0; i <= keyNum; i++) {
						children[i].parent = this;
					}
					System.arraycopy(parent.keys, 1, parent.keys, 0, parent.keyNum - 1);
					System.arraycopy(parent.children, 2, parent.children, 1, parent.keyNum - 1);
					parent.keyNum--;
					if(parent.keyNum < MinKeyNum)
						parent.borrowOrMergeFromSibling();
				}else {
					//Merge with left sibling
					InternalNode sibling = (InternalNode)parent.children[indexOfParent - 1];
					System.arraycopy(keys, 0, keys, sibling.keyNum + 1, keyNum);
					System.arraycopy(children, 0, children, sibling.keyNum + 1, keyNum + 1);
					keys[sibling.keyNum] = parent.keys[indexOfParent - 1];
					System.arraycopy(sibling.keys, 0, keys, 0, sibling.keyNum);
					System.arraycopy(sibling.children, 0, children, 0, sibling.keyNum + 1);
					keyNum += 1 + sibling.keyNum;
					for(int i = 0; i <= keyNum; i++) {
						children[i].parent = this;
					}
					System.arraycopy(parent.keys, indexOfParent, parent.keys, indexOfParent - 1, parent.keyNum - indexOfParent);
					System.arraycopy(parent.children, indexOfParent, parent.children, indexOfParent - 1, parent.keyNum - indexOfParent + 1);
					parent.keyNum--;
					if(parent.keyNum < MinKeyNum)
						parent.borrowOrMergeFromSibling();
				}
			}
		}
		
		private void insertFull(int key, Node child) {
			int index = getIndex(key);
			InternalNode sibling = new InternalNode();
			int newKeyForParent = 0;
			if(index <= keyNum - MinKeyNum - 1) {
				System.arraycopy(keys, keyNum - MinKeyNum, sibling.keys, 0, MinKeyNum);
				System.arraycopy(children, keyNum - MinKeyNum, sibling.children, 0, MinKeyNum + 1);
				newKeyForParent = keys[keyNum - MinKeyNum - 1];
				System.arraycopy(keys, index, keys, index + 1, keyNum - MinKeyNum - index - 1);
				System.arraycopy(children, index + 1, children, index + 2, keyNum - MinKeyNum - index - 1);
				keys[index] = key;
				children[index + 1] = child;
			}else if(index == keyNum - MinKeyNum) {
				System.arraycopy(keys, keyNum - MinKeyNum, sibling.keys, 0, MinKeyNum);
				System.arraycopy(children, keyNum - MinKeyNum + 1, sibling.children, 1, MinKeyNum);
				sibling.children[0] = child;
				newKeyForParent = key;
			}else {
				newKeyForParent = keys[keyNum - MinKeyNum];
				System.arraycopy(keys, keyNum - MinKeyNum + 1, sibling.keys, 0, index - keyNum + MinKeyNum - 1);
				System.arraycopy(children, keyNum - MinKeyNum + 1, sibling.children, 0, index - keyNum + MinKeyNum);
				sibling.keys[index - keyNum + MinKeyNum - 1] = key;
				sibling.children[index - keyNum + MinKeyNum] = child;
				System.arraycopy(keys, index, sibling.keys, index - keyNum + MinKeyNum, keyNum - index);
				System.arraycopy(children, index + 1, sibling.children, index - keyNum + MinKeyNum + 1, keyNum - index);
			}
			keyNum = keyNum - MinKeyNum;
			sibling.keyNum = MinKeyNum;
			//Update children
			for(int i = 0; i <= keyNum; i++) {
				children[i].parent = this;
			}
			for(int i = 0; i <= sibling.keyNum; i++) {
				sibling.children[i].parent = sibling;
			}
			//Check if a new root need to be generated
			if(parent == null) {
				InternalNode newRoot = new InternalNode();
				newRoot.keyNum = 1;
				newRoot.keys[0] = newKeyForParent;
				newRoot.children[0] = this;
				this.parent = newRoot;
				newRoot.children[1] = sibling;
				sibling.parent = newRoot;
				root = newRoot;
			}else {
				parent.insert(newKeyForParent, sibling);
			}
		}
		
		private void insertNotFull(int key, Node child) {
			int index = getIndex(key);
			System.arraycopy(keys, index, keys, index + 1, keyNum - index);
			System.arraycopy(children, index + 1, children, index + 2, keyNum - index);
			keys[index] = key;
			children[index + 1] = child;
			child.parent = this;
			keyNum++;
		}
		
		private int getIndex(int key) {
			for(int i = 0; i < keyNum; i++) {
				if(key < keys[i])
					return i;
			}
			return keyNum;
		}
		
	}
	
	class ExternalNode extends Node {
		
		double[] values;
		ExternalNode pre;
		ExternalNode after;
		
		ExternalNode(){
			super.keyNum = 0;
			super.keys = new int[MaxKeyNum];
			super.parent = null;
			this.values = new double[MaxKeyNum];
			this.pre = null;
			this.after = null;
		}
		
		public void insert(int key, double value) {
			if(keyNum == MaxKeyNum) {
				insertFull(key, value);
			}else {
				insertNotFull(key, value);
			}
		}
		
		public String search(int key) {
			for(int i = 0; i < keyNum; i++) {
				if(keys[i] == key)
					return new Double(values[i]).toString();
			}
			return "";
		}
		
		public String search(int startKey, int endKey) {
			StringBuilder sb = new StringBuilder();
			int index = getIndex(startKey);
			boolean startParse = false;
			if(index < keyNum) {
				for(int i = index; i < keyNum; i++) {
					if(keys[i] <= endKey) {
						if(!startParse) {
							sb.append(values[i]);
							startParse = true;
						}else {
							sb.append(",");
							sb.append(values[i]);
						}
					}else {
						return sb.toString();
					}
				}
			}
			ExternalNode nextNode = after;
			while(nextNode != null) {
				for(int i = 0; i < nextNode.keyNum; i++) {
					if(nextNode.keys[i] <= endKey) {
						if(!startParse) {
							sb.append(nextNode.values[i]);
							startParse = true;
						}else {
							sb.append(",");
							sb.append(nextNode.values[i]);
						}
					}else {
						return sb.toString();
					}
				}
				nextNode = nextNode.after;
			}
			return sb.toString();
		}
		
		public void delete(int key) {
			if(keyNum > MinKeyNum || parent == null) {
				deleteFromFatNode(key);
			}else {
				deleteWithMerge(key);
			}
		}
		
		public void display(Queue<Node> nextLevel) {
			for(int i = 0; i < keyNum; i++) {
				System.out.print(keys[i] + "(" + values[i] + ")");
				if(i != keyNum - 1)
					System.out.print(",");
			}
			if(parent == null)
				System.out.print("[!!!!!!!]");
			System.out.print("   ");
		}
		
		private void insertNotFull(int key, double value) {
			int index = getIndex(key);
			System.arraycopy(keys, index, keys, index + 1, keyNum - index);
			System.arraycopy(values, index, values, index + 1, keyNum - index);
			keys[index] = key;
			values[index] = value;
			keyNum++;
		}
		
		private void insertFull(int key, double value) {
			ExternalNode sibling = new ExternalNode();
			int index = getIndex(key);
			if(index < MinKeyNum) {
				System.arraycopy(keys, MinKeyNum - 1, sibling.keys, 0, keyNum - MinKeyNum + 1);
				System.arraycopy(values, MinKeyNum - 1, sibling.values, 0, keyNum - MinKeyNum + 1);
				sibling.keyNum = keyNum - MinKeyNum + 1;
				keyNum = MinKeyNum - 1;
				insertNotFull(key, value);
			}else {
				System.arraycopy(keys, MinKeyNum, sibling.keys, 0, keyNum - MinKeyNum);
				System.arraycopy(values, MinKeyNum, sibling.values, 0, keyNum - MinKeyNum);
				sibling.keyNum = keyNum - MinKeyNum;
				keyNum = MinKeyNum;
				sibling.insertNotFull(key, value);
			}
			//check next external node
			if(after != null) {
				sibling.after = after;
				after.pre = sibling;
			}
			after = sibling;
			sibling.pre = this;
			//check if the node to split is root
			if(parent == null) {
				InternalNode newRoot = new InternalNode();
				newRoot.keyNum = 1;
				newRoot.keys[0] = sibling.keys[0];
				newRoot.children[0] = this;
				parent = newRoot;
				newRoot.children[1] = sibling;
				sibling.parent = newRoot;
				root = newRoot;
			}else {
				sibling.parent = parent;
				parent.insert(sibling.keys[0], sibling);
			}
		}
		
		private void deleteFromFatNode(int key) {
			int index = -1;
			for(int i = 0; i < keyNum; i++) {
				if(keys[i] == key) {
					index = i;
				}
			}
			if(index == -1)
				return;
			System.arraycopy(keys, index + 1, keys, index, keyNum - index - 1);
			System.arraycopy(values, index + 1, values, index, keyNum - index - 1);
			keyNum--;
		}
		
		private void deleteWithMerge(int key) {
			int index = -1;
			for(int i = 0; i < keyNum; i++) {
				if(keys[i] == key) {
					index = i;
				}
			}
			if(index == -1)
				return;
			int indexOfParent = 0;
			for(int i = 0; i <= parent.keyNum; i++) {
				if(parent.children[i] == this) {
					indexOfParent = i;
					break;
				}
			}
			if(indexOfParent != 0 && parent.children[indexOfParent - 1].keyNum > MinKeyNum) {
				//Borrow an element from the left sibling
				ExternalNode sibling = (ExternalNode)parent.children[indexOfParent - 1];
				deleteFromFatNode(key);
				insertNotFull(sibling.keys[sibling.keyNum - 1], sibling.values[sibling.keyNum - 1]);
				sibling.keyNum--;
				parent.keys[indexOfParent - 1] = keys[0];
			}else if(indexOfParent != parent.keyNum && parent.children[indexOfParent + 1].keyNum > MinKeyNum) {
				//Borrow an element from the right sibling
				ExternalNode sibling = (ExternalNode)parent.children[indexOfParent + 1];
				deleteFromFatNode(key);
				insertNotFull(sibling.keys[0], sibling.values[0]);
				System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyNum - 1);
				System.arraycopy(sibling.values, 1, sibling.values, 0, sibling.keyNum - 1);
				sibling.keyNum--;
				parent.keys[indexOfParent] = sibling.keys[0];
			}else {
				//Start Merge
				if(indexOfParent == 0) {
					//Merge with the right sibling
					ExternalNode sibling = (ExternalNode)parent.children[indexOfParent + 1];
					deleteFromFatNode(key);
					System.arraycopy(sibling.keys, 0, keys, keyNum, sibling.keyNum);
					System.arraycopy(sibling.values, 0, values, keyNum, sibling.keyNum);
					keyNum += sibling.keyNum;
					if(sibling.after != null)
						sibling.after.pre = this;
					after = sibling.after;
					System.arraycopy(parent.keys, 1, parent.keys, 0, parent.keyNum - 1);
					System.arraycopy(parent.children, 2, parent.children, 1, parent.keyNum - 1);
					parent.keyNum--;
				}else {
					ExternalNode sibling = (ExternalNode)parent.children[indexOfParent - 1];
					deleteFromFatNode(key);
					System.arraycopy(keys, 0, keys, sibling.keyNum, keyNum);
					System.arraycopy(values, 0, values, sibling.keyNum, keyNum);
					System.arraycopy(sibling.keys, 0, keys, 0, sibling.keyNum);
					System.arraycopy(sibling.values, 0, values, 0, sibling.keyNum);
					keyNum += sibling.keyNum;
					if(sibling.pre != null)
						sibling.pre.after = this;
					pre = sibling.pre;
					System.arraycopy(parent.keys, indexOfParent, parent.keys, indexOfParent - 1, parent.keyNum - indexOfParent);
					System.arraycopy(parent.children, indexOfParent, parent.children, indexOfParent - 1, parent.keyNum - indexOfParent + 1);
					parent.keyNum--;
				}
				//Parent is deficient
				if(parent.keyNum < MinKeyNum)
					parent.borrowOrMergeFromSibling();
			}
		}
		
		private int getIndex(int key) {
			for(int i = 0; i < keyNum; i++) {
				if(key < keys[i]) {
					return i;
				}
			}
			return keyNum;
		}
		
	}
	
	public static void test() {
		bplustree tree = new bplustree(3);
		Set<Integer> insertSet = new HashSet<>();
		//int[] array = {33,36,59,2,48,35,43,5,34,64};
		for(int i = 1; i <= 1000; i++) {
			int insert = (int)(Math.random()*10000);
			//int insert = array[i - 1];
			System.out.println("Insert: " + insert);
			if(!insertSet.contains(insert)) {
				insertSet.add(insert);
				tree.insert(insert, insert);
			}
			//tree.displayWholeTree();
			//System.out.println();
		}
		tree.displayWholeTree();
		for(int insert : insertSet) {
			tree.delete(insert);
			System.out.println("\nDelete " + insert);
			tree.displayWholeTree();
		}
		tree.displayWholeTree();
	}
	
	public static void main(String[] args) {
		
	}
	
}
