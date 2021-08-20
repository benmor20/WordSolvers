package wordsolvers.structs;

import java.util.*;

public class DictionaryNode {
	private DictionaryNode parent;
	private Character parentPath;
	private final Map<Character, DictionaryNode> childMap;
	private boolean isWord;

	public DictionaryNode() {
		this(null, null);
	}
	public DictionaryNode(DictionaryNode parent, Character parentPath) {
		this(parent, parentPath, false);
	}
	public DictionaryNode(DictionaryNode parent, Character parentPath, boolean isWord) {
		this.parent = parent;
		this.parentPath = parentPath;
		this.childMap = new HashMap<>();
		this.isWord = isWord;
	}

	public boolean isWord() {
		return this.isWord;
	}
	public void setIsWord(boolean isWord) {
		this.isWord = isWord;
	}

	public DictionaryNode addChild(char path, boolean isWord) {
		this.childMap.put(path, new DictionaryNode(this, path, isWord));
		return this.childMap.get(path);
	}
	public DictionaryNode addChild(char path, DictionaryNode child) {
		this.childMap.put(path, child);
		child.parent = this;
		child.parentPath = path;
		return child;
	}

	public DictionaryNode getChild(char path) {
		if (path >= 'A' && path <= 'Z') path = (char)(path - 'A' + 'a'); // make lowercase
		return this.childMap.get(path);
	}
	public DictionaryNode getChild(String path) {
		return this.getChild(path.toCharArray());
	}
	public DictionaryNode getChild(char... path) {
		DictionaryNode currentNode = this;
		for (char c : path) {
			currentNode = currentNode.getChild(c);
			if (currentNode == null) return null;
		}
		return currentNode;
	}

	public Collection<DictionaryNode> getChildrenFrom(BlankSpace space) {
		Collection<DictionaryNode> ret = new LinkedHashSet<>();

		if (space.minBlanks == 0) {
			ret.add(this);
		}
		if (space.maxBlanks == 0) {
			return ret;
		}

		for (char c : space.possibleCharacters) {
			if (!this.hasChild(c)) continue;
			ret.addAll(this.getChild(c).getChildrenFrom(space.cloneOneLess()));
		}
		return ret;
	}

	public Set<Map.Entry<Character,DictionaryNode>> getChildren() {
		return this.childMap.entrySet();
	}

	public boolean hasChild(char path) {
		if (path >= 'A' && path <= 'Z') path = (char)(path - 'A' + 'a'); // make lowercase
		return this.childMap.get(path) != null;
	}
	public boolean hasChild(String path) {
		return this.hasChild(path.toCharArray());
	}
	public boolean hasChild(char[] path) {
		DictionaryNode currentNode = this;
		for (char c : path) {
			if (!currentNode.hasChild(c)) return false;
			currentNode = currentNode.getChild(c);
		}
		return true;
	}

	public boolean isTop() {
		return this.parent == null;
	}

	public DictionaryNode getParent() {
		return this.parent;
	}

	public DictionaryNode getRoot() {
		DictionaryNode currentNode = this;
		while (currentNode.parentPath != null) {
			currentNode = currentNode.parent;
		}
		return currentNode;
	}

	public int depth() {
		if (this.parent == null) return 0;
		return this.parent.depth() + 1;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		DictionaryNode currentNode = this;
		while (!currentNode.isTop()) {
			str.append(currentNode.parentPath);
			currentNode = currentNode.parent;
		}
		return str.reverse().toString();
	}
}
