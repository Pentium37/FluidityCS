package com.fluidity.program.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Queue<T> {
	protected T[] queue;
	protected int size;
	protected int headPointer;
	protected int MAX_SIZE;
	protected boolean maxSizeSet;

	public Queue() {
		queue = (T[]) new Object[20];
		this.MAX_SIZE = 20;
		maxSizeSet = false;
		headPointer = 0;
		size = 0;
	}

	public Queue(int MAX_SIZE) {
		this.MAX_SIZE = MAX_SIZE;
		queue = (T[]) new Object[MAX_SIZE];
		maxSizeSet = true;
		headPointer = 0;
		size = 0;
	}

	public void enqueue(T data) {
		extendSize();
		queue[(headPointer + size) % MAX_SIZE] = data;
		size++;
	}

	public T dequeue() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		T data = queue[headPointer];
		headPointer = (headPointer + 1) % MAX_SIZE;
		size--;
		return data;
	}

	public T peekQueue() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return queue[headPointer];
	}

	public void extendSize() {
		if (size == MAX_SIZE) {
			if (maxSizeSet) {
				throw new IllegalStateException();
			} else {
				T[] newQueue = (T[]) new Object[queue.length * 2];
				for (int i = 0; i < queue.length; i++) {
					newQueue[i] = queue[(headPointer + i) % queue.length];
				}
				queue = newQueue;
				headPointer = 0;
				MAX_SIZE = queue.length;
			}
		}
	}
	public boolean isEmpty() {
		return size == 0;
	}

	public boolean isFull() {
		return size == MAX_SIZE;
	}

	public void setMAX_SIZE(int MAX_SIZE) {
		if (MAX_SIZE < size) {
			throw new IllegalArgumentException();
		}
		this.MAX_SIZE = MAX_SIZE;
		T[] newQueue = (T[]) new Object[MAX_SIZE];
		for (int i = 0; i < size; i++) {
			newQueue[i] = queue[(headPointer + i) % queue.length];
		}
		queue = newQueue;
		headPointer = 0;
	}

	public int size() {
		return size;
	}

	public Queue<T> copy() {
		Queue<T> copy = new Queue<>(MAX_SIZE);
		copy.size = size;
		copy.headPointer = headPointer;
		copy.MAX_SIZE = MAX_SIZE;
		copy.maxSizeSet = maxSizeSet;
		copy.queue = queue.clone();
		return copy;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		output.append("[");
		for (int i = 0; i < size; i++) {
			output.append(queue[(headPointer + i) % MAX_SIZE]);
			if (i != size - 1) {
				output.append(", ");
			}
		}
		output.append("]\n");
		return output.toString();
	}
}