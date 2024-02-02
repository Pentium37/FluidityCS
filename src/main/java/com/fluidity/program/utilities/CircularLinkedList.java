package com.fluidity.program.utilities;

import javafx.scene.Node;

import java.util.NoSuchElementException;

public class CircularLinkedList<T> {
	public LinkedListNode<T> head;
	public LinkedListNode<T> tail;

	public CircularLinkedList() {
		head = null;
		tail = null;
	}

	public CircularLinkedList(T[] initalList) {
		for (T item : initalList) {
			addNode(item);
		}
	}

	public void addNode(T value) {
		LinkedListNode<T> newNode = new LinkedListNode<>(value);

		if (head == null) {
			head = newNode;
		} else {
			tail.nextNode = newNode;
		}

		tail = newNode;
		tail.nextNode = head;
	}

	public void deleteNode(T valueToDelete) {
		LinkedListNode<T> currentNode = head;
		if (head == null) {
			return;
		}
		do {
			LinkedListNode<T> nextNode = currentNode.nextNode;
			if (nextNode.payload == valueToDelete) {
				if (tail == head) {
					head = null;
					tail = null;
				} else {
					currentNode.nextNode = nextNode.nextNode;
					if (head == nextNode) {
						head = head.nextNode;
					}
					if (tail == nextNode) {
						tail = currentNode;
					}
				}
				break;
			}
			currentNode = nextNode;
		} while (currentNode != head);
	}

	public boolean containsNode(T searchValue) {
		LinkedListNode<T> currentNode = head;

		if (head != null) {
			do {
				if (currentNode.payload == searchValue) {
					return true;
				}
				currentNode = currentNode.nextNode;
			} while (currentNode != head);
		}
		return false;
	}

	public LinkedListNode<T> getNode(T searchValue) {
		LinkedListNode<T> currentNode = head;

		if (head != null) {
			do {
				if (currentNode.payload == searchValue) {
					return currentNode;
				}
				currentNode = currentNode.nextNode;
			} while (currentNode != head);
		}
		throw new NoSuchElementException();
	}
}
