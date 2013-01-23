package jp.angeworm.ensyuD.compiler;

import java.util.*;

public class RegisterStack {
	private Stack<Integer> stack;
	private List<Boolean>  register;
	private Deque<Integer> lastappend;
	
	public RegisterStack(int capacity) {
		stack = new Stack<Integer>();
		register = new ArrayList<Boolean>(capacity);
		for(int i = 0; i < capacity; i++) {
			register.add(false);
		}
		lastappend = new LinkedList<Integer>();
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
		return !stack.isEmpty();
	}
	
	public int free() {
		if(!stack.isEmpty()) {
			int index = stack.pop();
			lastappend.addFirst(index);
			lastappend.removeLast();
			return index;
		} else {
			
			int index = lastappend.removeLast();
			register.set(index, false);
			
			return index;
		}
	}
}
