package jp.angeworm.ensyuD.compiler;

import java.util.*;

public class RegisterStack {
	public Stack<Integer> stack;
	public List<Boolean>  register;
	public Deque<Integer> lastappend;
	
	public RegisterStack(int capacity) {
		stack = new Stack<Integer>();
		register = new ArrayList<Boolean>(capacity);
		for(int i = 0; i < capacity; i++) {
			register.add(false);
		}
		lastappend = new LinkedList<Integer>();
	}
	public RegisterStack(int capacity, boolean defaultValue) {
		stack = new Stack<Integer>();
		register = new ArrayList<Boolean>(capacity);
		lastappend = new LinkedList<Integer>();

		for(int i = 0; i < capacity; i++) {
			register.add(defaultValue);
			if(defaultValue){ 
				lastappend.add(i);
			}
		}
	}

	public boolean needStackPush() {
		return !register.contains(false);
	}

	public boolean needStackPush(int index) {
		return register.get(index);
	}
	
	public int take() {
		int index = register.indexOf(false);
		if(index == -1) {
			index = lastappend.remove();
			stack.add(index);
		}
		lastappend.add(index);
		register.set(index, true);
		return index;
	}
	
	public int take(int index) {
		if(register.get(index)) {
			lastappend.remove(index);
			stack.add(index);
		}
		lastappend.add(index);
		register.set(index, true);
		return index;
	}
	
	public boolean needStackPop() {
		//return !stack.isEmpty();
		return (!stack.empty()) && (stack.peek() == lastappend.peekLast());
	}
	public boolean needStackPop(int i) {
		//return !stack.isEmpty();
		return (!stack.empty()) && (stack.peek() == i);
	}
	
	public int free() {
		if(!stack.isEmpty()) {
			int index = stack.pop();
			
			if(lastappend.contains(index)) {
				lastappend.remove(index);
			}
			lastappend.addFirst(index);

			return index;
		} else {
			
			int index = lastappend.removeLast();
			register.set(index, false);
			
			return index;
		}
	}
	public int free(int i) {
		if(!stack.isEmpty() && stack.peek() == i ) {
			int index = stack.pop();
			
			if(lastappend.contains(index)) {
				lastappend.remove(index);
			}	
			lastappend.addFirst(index);
		
			return index;
		} else {
			lastappend.remove(i);
			register.set(i, false);
			
			return i;
		}
	}
	
	public int getStackHeight() {
		return stack.size();
	}
}
