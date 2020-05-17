package hr.fer.zemris.ooup.lab3.texteditor;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SelectionTool implements KeyListener {
	
	private TextEditorModel model;

	public SelectionTool(TextEditorModel model) {
		this.model = model;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		
		boolean isSelected = model.isSelected();
		if (!isSelected) {
			model.setSelectionStart(model.getCursorLocation()); // also sets isSelected = true
			model.setSelectionEnd(model.getCursorLocation());
			return;
		}
			
		if (c == KeyEvent.VK_UP) {
			model.moveCursorUp();
			model.setSelectionEnd(model.getCursorLocation());
			
		} else if (c == KeyEvent.VK_DOWN) {
			model.moveCursorDown();
			model.setSelectionEnd(model.getCursorLocation());
			
		} else if (c == KeyEvent.VK_LEFT) {
			model.moveCursorLeft();
			model.setSelectionEnd(model.getCursorLocation());

			
		} else if (c == KeyEvent.VK_RIGHT) {
			model.moveCursorRight();
			model.setSelectionEnd(model.getCursorLocation());
			

		} else if (c == KeyEvent.VK_BACK_SPACE) {
			model.deleteRange(model.getSelectionRange());
			model.resetSelection();
			
		} else if (c == KeyEvent.VK_DELETE) {
			model.deleteRange(model.getSelectionRange());
			model.resetSelection();
			
		} else {
			char a = e.getKeyChar();
			if (a != KeyEvent.CHAR_UNDEFINED) {
				model.insert(a);
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
