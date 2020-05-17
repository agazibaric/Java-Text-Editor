package hr.fer.zemris.ooup.lab3.texteditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UndoManager {
	
	private Stack<EditAction> undoStack = new Stack<>();
	private Stack<EditAction> redoStack = new Stack<>();
	private List<UndoObserver> observers = new ArrayList<>();
	
	public UndoManager() {
	}
	
	public void addObserver(UndoObserver o) {
		observers.add(o);
	}
	
	public void removeObserver(UndoObserver o) {
		observers.remove(o);
	}
	
	private void notifyAllObservers() {
		for (UndoObserver o : new ArrayList<>(observers)) {
			o.undoStackStatus(undoStack.isEmpty());
			o.redoStackStatus(redoStack.isEmpty());
		}
	}
	
	void undo() {
		if (!undoStack.isEmpty()) {
			EditAction a = undoStack.pop();
			redoStack.push(a);
			a.execute_undo();
			
			notifyAllObservers();
		}
	}
	
	void redo() {
		if (!redoStack.isEmpty()) {
			EditAction a = redoStack.pop();
			undoStack.push(a);
			a.execute_do();
			
			notifyAllObservers();
		}
	}
	
	void push(EditAction c) {
		redoStack.clear();
		undoStack.push(c);
		
		notifyAllObservers();
	}

}
