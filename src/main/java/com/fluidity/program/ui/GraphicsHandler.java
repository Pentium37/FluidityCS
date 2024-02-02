package com.fluidity.program.ui;

import com.fluidity.program.utilities.CircularLinkedList;
import com.fluidity.program.utilities.LinkedListNode;

public class GraphicsHandler {
	private LinkedListNode<Integer> currentFPS;
	private LinkedListNode<int[]> currentCellSize;
	private LinkedListNode<Integer> currentIterations;

	public GraphicsHandler(int currentFPS, int[] currentCellSize, int currentIterations) {
		Integer[] fpsValues = { 15, 30, 45, 60, 75 };
		CircularLinkedList<Integer> fpsLinkedList = new CircularLinkedList<>(fpsValues);
		if (fpsLinkedList.containsNode(currentFPS)) {
			this.currentFPS = fpsLinkedList.getNode(currentFPS);
		} else {
			this.currentFPS = fpsLinkedList.head;
		}

		int[][] cellSizeValues = { { 1, 1 }, { 2, 2 }, { 3, 3 }, { 4, 4 }, { 5, 5 } };
		CircularLinkedList<int[]> cellSizeLinkedList = new CircularLinkedList<>(cellSizeValues);
		if (cellSizeLinkedList.containsNode(currentCellSize)) {
			this.currentCellSize = cellSizeLinkedList.getNode(currentCellSize);
		} else {
			this.currentCellSize = cellSizeLinkedList.head;
		}

		Integer[] iterationsValues = { 4, 8, 12, 16, 20, 24, 28, 32, 36, 40 };
		CircularLinkedList<Integer> iterationsLinkedList = new CircularLinkedList<>(iterationsValues);
		if (iterationsLinkedList.containsNode(currentIterations)) {
			this.currentIterations = iterationsLinkedList.getNode(currentIterations);
		} else {
			this.currentIterations = iterationsLinkedList.head;
		}
	}

	public int getCurrentFPS() {
		return currentFPS.payload;
	}

	public int[] getCurrentCellSize() {
		return currentCellSize.payload;
	}

	public int getCurrentIterations() {
		return currentIterations.payload;
	}

	public void shiftIterations() {
		currentIterations = currentIterations.nextNode;
	}

	public void shiftCellSize() {
		currentCellSize = currentCellSize.nextNode;
	}

	public void shiftFPS() {
		currentFPS = currentFPS.nextNode;
	}
}
