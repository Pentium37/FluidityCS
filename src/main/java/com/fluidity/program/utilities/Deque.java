package com.fluidity.program.utilities;

import java.util.NoSuchElementException;

public class Deque<T> extends Queue<T> {
	public Deque() {
		super();
	}

	public Deque(int MAX_SIZE) {
		super(MAX_SIZE);
	}

	public T pop() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		T data = queue[(headPointer + size - 1) % MAX_SIZE];
		size--;
		return data;
	}

	public void retrievePush(){
		if (!isFull() && queue[(headPointer + size) % MAX_SIZE] != null) {
			size++;
		}
	}

	public T peekStack() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return queue[(headPointer + size - 1) % MAX_SIZE];
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
