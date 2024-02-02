package com.fluidity.program.utilities;

public class LinkedListNode<T> {
	public T payload;
	public LinkedListNode<T> nextNode;

	public LinkedListNode(final T payload, final LinkedListNode<T> nextNode) {
		this.payload = payload;
		this.nextNode = nextNode;
	}

	public LinkedListNode(final T payload) {
		this.payload = payload;
		this.nextNode = null;
	}

	public LinkedListNode() {
		this.payload = null;
		this.nextNode = null;
	}
}
