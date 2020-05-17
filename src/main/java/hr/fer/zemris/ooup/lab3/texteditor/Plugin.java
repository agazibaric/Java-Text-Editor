package hr.fer.zemris.ooup.lab3.texteditor;

public interface Plugin {

	String getName(); // name of plugin

	String getDescription(); // short description

	void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack);

}
