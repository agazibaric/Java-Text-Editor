package hr.fer.zemris.ooup.lab3.texteditor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

public class TextEditor extends JComponent implements CursorObserver, TextObserver, KeyListener {
	
	
	private static final long serialVersionUID = 1L;
	private static KeyListener CURSOR_TOOL;
	private static KeyListener SELECTION_TOOL;
	
	TextEditorModel model;
	ClipboardStack clipboard = new ClipboardStack();
	private KeyListener currentTool;
	private Color backgroundColor = Color.WHITE;
	private Color selectedColor = Color.BLUE;
	private Color selectedLineColor = Color.WHITE;
	private Color lineColor = Color.BLACK;
	private Color cursorColor = Color.BLACK;
	private int fontHeight = 12;
	private String fontName = "TimesRoman";
	private Font font;
	private int dy = (int)(fontHeight + fontHeight / 3.0);
	private int dx = 5;
	private boolean isCursorVisible = true;
	
	private List<Integer> pressedKeys = new ArrayList<>();
	
	
	public TextEditor() {
		List<String> lines = new ArrayList<>();
		lines.add("");

		model = new TextEditorModel(lines);
		model.setCursorLocation(new Location(0, 0));
		model.addCursorObserver(this);
		model.addTextObserver(this);
		font = new Font(fontName, Font.PLAIN, fontHeight);
		
		CURSOR_TOOL = new CursorTool(model);
		SELECTION_TOOL = new SelectionTool(model);
		currentTool = CURSOR_TOOL;
		
		activateClock();
	}
	
	
	public void clearAll() {
		model.clearAll();
	}
	
	public void addNewText(String text) {
		model.insert(text);
	}

	public String getContent() {
		return model.getAllContent();
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setFont(font);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		drawLines(g2d);
		drawCursor(g2d);
	}
	
	private void activateClock() {
		Thread clockThread = new Thread(() -> {
			while (true) {
				try {
					isCursorVisible = !isCursorVisible;
					repaint();
					Thread.sleep(600);
				} catch (InterruptedException ignorable) {
				}
			}
		});
		clockThread.setDaemon(true);
		clockThread.start();
	}
	
	private void drawCursor(Graphics2D g2d) {
		if (isCursorVisible) {
			Location l = model.getCursorLocation();
			g2d.setColor(cursorColor);
			int width = getStringWidth(g2d, model.getTextBeforeCursor());
			g2d.drawLine(width + dx, l.y * dy, width + dx, l.y * dy + dy);
		}
	}
	
	private void drawLines(Graphics2D g2d) {
		FontMetrics fm = g2d.getFontMetrics();
		int x = dx;
		int y = dy;
		g2d.setColor(lineColor);
		g2d.setBackground(backgroundColor);
		
		if (model.isSelected()) {
			List<String> lines = model.getLines();
			
			LocationRange r = model.getSelectionRange().copy();
			model.normalizeLocationRange(r);
			for (int i = 0, n = lines.size(); i < n; i++) {
				if (i == r.l1.y) {
					if (i == r.l2.y) {
						String s = lines.get(i);
						String notSelected1 = s.substring(0, r.l1.x);
						String selected = s.substring(r.l1.x, r.l2.x);
						String notSelected2 = s.substring(r.l2.x, s.length());
						int w1 = getStringWidth(g2d, notSelected1);
						int wsel = getStringWidth(g2d, selected);
						
						g2d.setColor(lineColor);
						g2d.drawString(notSelected1, x, y);
						
						Rectangle2D rect = fm.getStringBounds(selected, g2d);
		                g2d.setColor(selectedColor);
						g2d.fillRect(x + w1, y - fm.getAscent(), (int) rect.getWidth(), (int) rect.getHeight());
						
						g2d.setColor(selectedLineColor);
						g2d.drawString(selected, x + w1, y);
						
						g2d.setColor(lineColor);
						g2d.drawString(notSelected2, x + w1 + wsel, y);
						
					} else {
					
						String s = lines.get(i);
						String notSelected = s.substring(0, r.l1.x);
						String selected = s.substring(r.l1.x, s.length());
						int w1 = getStringWidth(g2d, notSelected);
						
						g2d.setColor(lineColor);
						g2d.drawString(notSelected, x, y);
						
						Rectangle2D rect = fm.getStringBounds(selected, g2d);
		                g2d.setColor(selectedColor);
						g2d.fillRect(x + w1, y - fm.getAscent(), (int) rect.getWidth(), (int) rect.getHeight());
						
						g2d.setColor(selectedLineColor);
						g2d.drawString(selected, x + w1, y);
					}
					
				} else if (i == r.l2.y) {
					
					String s = lines.get(i);
					String selected = s.substring(0, r.l2.x);
					String notSelected = s.substring(r.l2.x, s.length());
					int wsel = getStringWidth(g2d, selected);
					
					Rectangle2D rect = fm.getStringBounds(selected, g2d);
	                g2d.setColor(selectedColor);
					g2d.fillRect(x, y - fm.getAscent(), (int) rect.getWidth(), (int) rect.getHeight());
						
					
					g2d.setColor(selectedLineColor);
					g2d.drawString(selected, x, y);
					
					g2d.setColor(lineColor);
					g2d.drawString(notSelected, x + wsel, y);
					
				} else if (i > r.l1.y && i < r.l2.y) {
					
					String s = lines.get(i);
					
					Rectangle2D rect = fm.getStringBounds(s, g2d);
	                g2d.setColor(selectedColor);
					g2d.fillRect(x, y - fm.getAscent(), (int) rect.getWidth(), (int) rect.getHeight());
					
					g2d.setColor(selectedLineColor);
					g2d.drawString(s, x, y);
					
				} else {
					
					String s = lines.get(i);
					g2d.setColor(lineColor);
					g2d.drawString(s, x, y);
					
				}
				y += dy;
			}
			
		} else {
			Iterator<String> it = model.allLines();
			while(it.hasNext()) {
				g2d.drawString(it.next(), x, y);
				y += dy;
			}
		}
	}
	
	public void undo() {
		model.undoAction();
	}

	public void redo() {
		model.redoAction();
	}
	
	public void pasteAndTake() {
		String s = clipboard.pop();
		model.insert(s);
	}
	
	public void paste() {
		String s = clipboard.peek();
		model.insert(s);
	}
	
	public void copy() {
		String s = model.getSelectedText();
		clipboard.push(s);
	}
	
	public void cut() {
		String s = model.getSelectedText();
		clipboard.push(s);
		model.deleteRange(model.getSelectionRange());
		model.resetSelection();
	}
	
	public void deleteSelection() {
		if (model.isSelected()) {
			model.deleteRange(model.getSelectionRange());
			model.resetSelection();
		}
	}
	
	public void clearDocument() {
		model.clearAll();
	}
	
	public void moveCursorToStart() {
		model.setCursorLocation(TextEditorModel.DEFAULT_LOCATION);
	}
	
	public void moveCursorToEnd() {
		model.moveCursorToEnd();
	}
	
	
	private int getStringWidth(Graphics2D g2d, String s) {
		return g2d.getFontMetrics().stringWidth(s);
	}

	@Override
	public void updateCursorLocation(Location loc) {
		this.repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Integer shiftDown = KeyEvent.SHIFT_DOWN_MASK;
		Integer ctrlDown = KeyEvent.CTRL_DOWN_MASK;
		int c = e.getKeyCode();
		
		if ((e.getModifiersEx() & ctrlDown) == ctrlDown) {
			
			if (c == KeyEvent.VK_C && model.isSelected()) {
				String s = model.getSelectedText();
				
				clipboard.push(s);
				
			} else if (c == KeyEvent.VK_X && model.isSelected()) {
				String s = model.getSelectedText();
				clipboard.push(s);
				model.deleteRange(model.getSelectionRange());
				model.resetSelection();
				
			} else if (c == KeyEvent.VK_V && !clipboard.isEmpty()) {
				
				if ((e.getModifiersEx() & shiftDown) == shiftDown) {
					String s = clipboard.pop();
					//System.out.println("POP: " + s);
					model.insert(s);
				} else {
					String s = clipboard.peek();
					//System.out.println("POP: " + s);
					model.insert(s);
					
				}
			} if (c == KeyEvent.VK_Z) {
				
				model.undoAction();
			} if (c == KeyEvent.VK_Y) {
				model.redoAction();
			}
			
			return;
		} else if ((e.getModifiersEx() & shiftDown) == shiftDown) {
			if (!pressedKeys.contains(shiftDown)) {
				pressedKeys.add(shiftDown);
				currentTool = SELECTION_TOOL;
			}

		} else if (!pressedKeys.contains(shiftDown)) {
			if (c == KeyEvent.VK_DELETE || c == KeyEvent.VK_BACK_SPACE) {
				currentTool.keyPressed(e);
				currentTool = CURSOR_TOOL;
				model.resetSelection();
				return;
			}
			
			currentTool = CURSOR_TOOL;
			model.resetSelection();
		}
		
		
		currentTool.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Integer shiftDown = KeyEvent.SHIFT_DOWN_MASK;
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			if (pressedKeys.contains(shiftDown)) {
				pressedKeys.remove(shiftDown);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}




	@Override
	public void updateText() {
		this.repaint();
	}
	
	

}
