package com.fluidity.program.ui;

import com.fluidity.program.utilities.CircularLinkedList;
import com.fluidity.program.utilities.LinkedListNode;

public class GraphicsHandler {
	private final CircularLinkedList<Integer> fpsLinkedList;
	private final CircularLinkedList<int[]> cellSizeLinkedList;
	private final CircularLinkedList<int[]> fluidSizeLinkedList;

	private LinkedListNode<Integer> currentFPS;
	private LinkedListNode<int[]> currentCellSize;
	private LinkedListNode<int[]> currentFluidSize;

	public GraphicsHandler(int currentFPS, int[] currentCellSize, int[] currentFluidSize) {
		Integer[] fpsValues = { 15, 30, 45, 60, 75 };
		fpsLinkedList = new CircularLinkedList<>(fpsValues);
		if (fpsLinkedList.containsNode(currentFPS)) {
			this.currentFPS = fpsLinkedList.getNode(currentFPS);
		} else {
			this.currentFPS = fpsLinkedList.head;
		}

		int[][] cellSizeValues = { { 1, 1 }, { 2, 2 }, { 3, 3 }, { 4, 4 }, { 5, 5 } };
		cellSizeLinkedList = new CircularLinkedList<>(cellSizeValues);
		if (cellSizeLinkedList.containsNode(currentCellSize)) {
			this.currentCellSize = cellSizeLinkedList.getNode(currentCellSize);
		} else {
			this.currentCellSize = cellSizeLinkedList.head;
		}

		int[][] fluidSizeValues = { { 100, 100 }, { 200, 200 }, { 300, 300 }, { 400, 400 } };
		fluidSizeLinkedList = new CircularLinkedList<>(fluidSizeValues);
		if (fluidSizeLinkedList.containsNode(currentFluidSize)) {
			this.currentFluidSize = cellSizeLinkedList.getNode(currentFluidSize);
		} else {
			this.currentFluidSize = fluidSizeLinkedList.head;
		}
	}

	public int getCurrentFPS() {
		return currentFPS.payload;
	}

	public int[] getCurrentCellSize() {
		return currentCellSize.payload;
	}

	public int[] getCurrentFluidSize() {
		return currentFluidSize.payload;
	}

	public void shiftFluidSize() {
		currentFluidSize = currentFluidSize.nextNode;
	}

	public void shiftCellSize() {
		currentCellSize = currentCellSize.nextNode;
	}

	public void shiftFPS() {
		currentFPS = currentFPS.nextNode;
	}
}
