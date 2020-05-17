package hr.fer.zemris.ooup.lab3.texteditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class ClipboardStack {

	
	private Stack<String> texts = new Stack<>();
	private List<ClipboardObserver> observers = new ArrayList<>();
	
	public ClipboardStack() {
			
	}
	
	public void notifyObservers() {
		for (ClipboardObserver o : new ArrayList<>(observers)) {
			o.updateClipboard(texts.isEmpty());
		}
	}
	
	public void addObserver(ClipboardObserver o) {
		observers.add(o);
	}
	
	public void removeObserver(ClipboardObserver o) {
		observers.remove(o);
	}
	
	public void push(String s) {
		texts.push(s);
		notifyObservers();
	}
	
	public String pop() {
		String s = texts.pop();
		notifyObservers();
		return s;
	}
	
	public String peek() {
		String s = texts.peek();
		//notifyObservers();
		return s;
	}
	
	public boolean isEmpty() {
		return texts.isEmpty();
	}
	
	public void clear() {
		texts.clear();
		notifyObservers();
	}
	
	
	
}
