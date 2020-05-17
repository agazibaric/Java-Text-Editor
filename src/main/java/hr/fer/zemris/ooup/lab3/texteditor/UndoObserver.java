package hr.fer.zemris.ooup.lab3.texteditor;

public interface UndoObserver {
	
	void undoStackStatus(boolean isStackEmpty);
	void redoStackStatus(boolean isStackEmpty);

}
