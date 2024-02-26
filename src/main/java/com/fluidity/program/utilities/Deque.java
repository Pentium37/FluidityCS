package com.fluidity.program.utilities;

import java.util.NoSuchElementException;

public class Deque<T> extends Queue<T> {
	public Deque() {
		super();
	}

	public Deque(int MAX_SIZE) {
		super(MAX_SIZE);
	}

	public void push(T data) {
		extendSize();
		headPointer = (headPointer - 1 + MAX_SIZE) % MAX_SIZE;
		queue[headPointer] = data;
		size++;
	}

	public T pop() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		T data = queue[(headPointer + size - 1) % MAX_SIZE];
		size--;
		return data;
	}
}
