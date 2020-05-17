package hr.fer.zemris.ooup.lab3.texteditor;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CursorTool implements KeyListener {
	
	private TextEditorModel model;

	public CursorTool(TextEditorModel model) {
		this.model = model;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		
		if (c == KeyEvent.VK_UP) {
			model.moveCursorUp();
		} else if (c == KeyEvent.VK_DOWN) {
			model.moveCursorDown();
		} else if (c == KeyEvent.VK_LEFT) {
			model.moveCursorLeft();
		} else if (c == KeyEvent.VK_RIGHT) {
			model.moveCursorRight();
		} else if (c == KeyEvent.VK_BACK_SPACE) {
			model.deleteBefore();
		} else if (c == KeyEvent.VK_DELETE) {
			model.deleteAfter();
		} else if (c == KeyEvent.VK_ENTER){
			
			model.insertEnter();
			
		} else {
			
			char a = e.getKeyChar();
			if (a != KeyEvent.CHAR_UNDEFINED) {
				model.insert(a);
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// not needed
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// not needed		
	}

}
