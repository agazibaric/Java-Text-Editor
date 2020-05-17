package hr.fer.zemris.ooup.lab3.texteditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextEditorModel {
	
	public static final Location DEFAULT_LOCATION = new Location(0, 0);
	public static final LocationRange DEFAULT_SELECTION_RANGE = new LocationRange(new Location(0,0), new Location(0,0));
	
	UndoManager undoManager = new UndoManager();
	private List<String> lines = new ArrayList<>();
	private LocationRange selectionRange = DEFAULT_SELECTION_RANGE;
	private Location cursorLocation = DEFAULT_LOCATION;
	private List<CursorObserver> cursorObservers = new ArrayList<>();
	private List<TextObserver> textObservers = new ArrayList<>();
	private List<SelectionObserver> selectionObservers = new ArrayList<>();
	private boolean isSelected = false;
	
	
	public TextEditorModel(List<String> lines) {
		this.lines = lines;
	}
	
	public void forceNotify() {
		notifyTextObservers();
	}
	
	public String getSelectedText() {
		if (isSelected) {
			LocationRange r = selectionRange.copy();
			normalizeLocationRange(r);
			StringBuilder sb = new StringBuilder();
			
			if (r.l1.y == r.l2.y) {
				sb.append(lines.get(r.l1.y).subSequence(r.l1.x, r.l2.x));
			} else {
				for (int i = r.l1.y; i <= r.l2.y; i++) {
					
					if (i == r.l1.y) {
						String s = lines.get(i);
						sb.append(s.substring(r.l1.x, s.length()));
						sb.append("\n");
					} else if (i == r.l2.y) {
						String s = lines.get(i);
						sb.append(s.substring(0, r.l2.x));
					} else {
						sb.append(lines.get(i));
						sb.append("\n");
					}
				}
				
			}
			return sb.toString();
		}
		
		return "";
	}
	
	public void insertEnter() {
		EditAction action = new EditAction() {
			
			private List<String> oldLines = new ArrayList<>(lines);
			private Location oldCursor = cursorLocation.copy();
			
			
			@Override
			public void execute_undo() {
				lines = new ArrayList<>(oldLines);
				cursorLocation = oldCursor.copy();
				notifyCursorObservers();
				notifyTextObservers();
			}
			
			@Override
			public void execute_do() {
				String line = lines.get(cursorLocation.y);
				String firstPart = line.substring(0, cursorLocation.x);
				String secondPart = line.substring(cursorLocation.x, line.length());
				
				lines.set(cursorLocation.y, firstPart);
				lines.add(cursorLocation.y + 1, secondPart);
				cursorLocation.x = 0;
				cursorLocation.y++;
				notifyCursorObservers();
				notifyTextObservers();
			}
		};
	
		action.execute_do();
		undoManager.push(action);
	}
	
	public String getAllContent() {
		if (lines.isEmpty())
			return "";
		
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public void undoAction() {
		undoManager.undo();
	}
	
	public void redoAction() {
		undoManager.redo();
	}
	
	public void clearAll() {
		lines.clear();
		lines.add("");
		resetSelection();
		cursorLocation = DEFAULT_LOCATION.copy();
		notifyCursorObservers();
		notifyTextObservers();
	}
	
	public void moveCursorToEnd() {
		int row = lines.size() - 1;
		int col = lines.get(row).length();
		
		cursorLocation.x = col;
		cursorLocation.y = row;
		
		notifyCursorObservers();
	}
	
	public void insert(char c) {
		EditAction action = new EditAction() {
			
			private List<String> oldLines = new ArrayList<>(lines);
			private Location oldCursor = cursorLocation.copy();
			
			
			@Override
			public void execute_undo() {
				lines = new ArrayList<>(oldLines);
				cursorLocation = oldCursor.copy();
				notifyCursorObservers();
				notifyTextObservers();
			}
			
			@Override
			public void execute_do() {
				String line = oldLines.get(cursorLocation.y);
				StringBuilder sb = new StringBuilder(line);
				sb.insert(cursorLocation.x, c);
				lines.set(cursorLocation.y, sb.toString());
				cursorLocation.x++;
				
				
				notifyCursorObservers();
				notifyTextObservers();
				
			}
		};
		
		action.execute_do();
		undoManager.push(action);
	}
	
	public void insert(String text) {
		
		EditAction action = new EditAction() {
			
			private List<String> oldLines = new ArrayList<>(lines);
			private Location oldCursor = cursorLocation.copy();
			
			@Override
			public void execute_undo() {
				lines = new ArrayList<>(oldLines);
				cursorLocation = oldCursor.copy();
				notifyCursorObservers();
				notifyTextObservers();
			}
			
			@Override
			public void execute_do() {
				String newLines[] = text.split("\\n");
				String existingLine = lines.get(cursorLocation.y);
				String firstPart = existingLine.substring(0, cursorLocation.x);
				String secondPart = existingLine.substring(cursorLocation.x, existingLine.length());
				
				for (int newIndex = 0, lineIndex = cursorLocation.y, n = newLines.length; newIndex < n; newIndex++, lineIndex++) {
					if (lineIndex == cursorLocation.y) {
						if (n == 1) {
							String newLine = firstPart.concat(newLines[newIndex]);
							int xpos = newLine.length();
							newLine = newLine.concat(secondPart);
							lines.set(lineIndex, newLine);
							cursorLocation.x = xpos;
						} else {
							String newLine = firstPart.concat(newLines[newIndex]);
							lines.set(lineIndex, newLine);
						}
					} else if (newIndex == n - 1) {
						String newLine = newLines[newIndex];
						int xpos = newLine.length();
						newLine = newLine.concat(secondPart);
						lines.add(lineIndex, newLine);
						cursorLocation.y = lineIndex;
						cursorLocation.x = xpos;
					} else {
						lines.add(lineIndex, newLines[newIndex]);
					}
					
				}
				
				notifyCursorObservers();
				notifyTextObservers();
				
			}
		};
		
		action.execute_do();
		undoManager.push(action);
	}
	
	public void resetSelection() {
		selectionRange = DEFAULT_SELECTION_RANGE.copy();
		isSelected = false;
		notifySelectionObservers();
	}
	
	public boolean isSelected() {
		return isSelected;
	}
	
	public void deleteBefore() {
		EditAction action = new EditAction() {
			
			private List<String> oldLines = new ArrayList<>(lines);
			private Location oldCursor = cursorLocation.copy();
			
			@Override
			public void execute_undo() {
				lines = new ArrayList<>(oldLines);
				cursorLocation = oldCursor.copy();
				notifyCursorObservers();
				notifyTextObservers();
			}
			
			@Override
			public void execute_do() {
				if (cursorLocation.x > 0) {
					StringBuilder sb = new StringBuilder(lines.get(cursorLocation.y));
					sb.deleteCharAt(cursorLocation.x - 1);
					lines.set(cursorLocation.y, sb.toString());
					cursorLocation.x--;
					notifyCursorObservers();
					notifyTextObservers();
				} else if (cursorLocation.y > 0){
					if (lines.get(cursorLocation.y).isEmpty()) {
						lines.remove(cursorLocation.y);
						cursorLocation.y--;
						cursorLocation.x = lines.get(cursorLocation.y).length();
					} else {
						String line = lines.get(cursorLocation.y);
						lines.remove(cursorLocation.y);
						String previosLine = lines.get(cursorLocation.y - 1);
						String concatedLine = previosLine.concat(line);
						lines.set(cursorLocation.y - 1, concatedLine);
						cursorLocation.y--;
						cursorLocation.x = previosLine.length();
					}
					notifyCursorObservers();
					notifyTextObservers();
				}
				
			}
		};
		
		action.execute_do();
		undoManager.push(action);
		
	}
	
	public void deleteAfter() {
		EditAction action = new EditAction() {
			
			private List<String> oldLines = new ArrayList<>(lines);
			
			@Override
			public void execute_undo() {
				lines = new ArrayList<>(oldLines);
				notifyTextObservers();
			}
			
			@Override
			public void execute_do() {
				String currentLine = lines.get(cursorLocation.y);
				if (cursorLocation.x < currentLine.length()) {
					StringBuilder sb = new StringBuilder(currentLine);
					sb.deleteCharAt(cursorLocation.x);
					lines.set(cursorLocation.y, sb.toString());
					notifyTextObservers();
				}
			}
		};
		
		action.execute_do();
		undoManager.push(action);
	}
	
	public void deleteRange(LocationRange range) {
		if (range.l1.equals(range.l2))
			return;
		
		EditAction action = new EditAction() {
			
			private LocationRange r = range;
			private List<String> oldLines = new ArrayList<>(lines);
			private Location oldCursor = cursorLocation.copy();
			
			@Override
			public void execute_undo() {
				lines = new ArrayList<>(oldLines);
				cursorLocation = oldCursor.copy();
				notifyCursorObservers();
				notifyTextObservers();
			}
			
			@Override
			public void execute_do() {
				// Switch locations if they are in revered order
				r = range.copy();
				normalizeLocationRange(r);
				
				// Make changes for each selected line in given range
				for (int i = r.l2.y; i >= r.l1.y; i--) {
					if (i != r.l2.y && i != r.l1.y) {
						lines.remove(i);
					} else if (i == r.l2.y) {
						if (i == r.l1.y) {
							String currentLine = lines.get(i);
							StringBuilder sb = new StringBuilder(currentLine);
							sb.delete(r.l1.x, r.l2.x);
							lines.set(i, sb.toString());
							
						} else {
							String currentLine = lines.get(i);
							StringBuilder sb = new StringBuilder(currentLine);
							sb.delete(0, r.l2.x);
							if (sb.length() == 0) {
								
								lines.remove(i);
							} else {
								lines.set(i, sb.toString());
							}
						}
						
					} else {
						String currentLine = lines.get(i);
						StringBuilder sb = new StringBuilder(currentLine);
						sb.delete(r.l1.x, currentLine.length());
						lines.set(i, sb.toString());
						
					}
				}
				
				cursorLocation.setLocation(r.l1.copy());
				resetSelection();
				notifyTextObservers();
				notifyCursorObservers();
				
			}
		};
		
		action.execute_do();
		undoManager.push(action);
	}
	
	public String getTextBeforeCursor() {
		return getTextBeforeLocation(cursorLocation);
	}
	
	public String getTextBeforeLocation(Location l) {
		String s = lines.get(l.y);
		return s.substring(0, l.x);
	}
	
	public String getTextAfterLocation(Location l) {
		String s = lines.get(l.y);
		return s.substring(l.x, s.length());
	}
	
	public void moveCursorLeft() {

		if (cursorLocation.x > 0) {
			cursorLocation.x--;
			notifyCursorObservers();
		}

	}
	
	public void moveCursorRight() {
		if (cursorLocation.y < lines.size()) {
			String s = lines.get(cursorLocation.y);
			if (cursorLocation.x < s.length()) {
				cursorLocation.x++;
				notifyCursorObservers();
			}
		}
	}
	
	public void moveCursorUp() {
		if (cursorLocation.y > 0) {
			cursorLocation.y--;
			String s = lines.get(cursorLocation.y);
			if (cursorLocation.x > s.length()) {
				cursorLocation.x = s.length();
			}
			notifyCursorObservers();
		}
	}
	
	public void moveCursorDown() {
		if (cursorLocation.y < lines.size() - 1) {
			cursorLocation.y++;
			String s = lines.get(cursorLocation.y);
			if (cursorLocation.x > s.length()) {
				cursorLocation.x = s.length();
			}
			notifyCursorObservers();
		}
	}
	
	private void notifyTextObservers() {
		for (TextObserver c : new ArrayList<>(textObservers)) {
			c.updateText();
		}
	}
	
	public void addTextObserver(TextObserver c) {
		textObservers.add(c);
	}
	
	public void removeTextObserver(TextObserver c) {
		textObservers.remove(c);
	}
	
	private void notifyCursorObservers() {
		for (CursorObserver c : new ArrayList<>(cursorObservers)) {
			c.updateCursorLocation(cursorLocation);
		}
	}
	
	public void addCursorObserver(CursorObserver c) {
		cursorObservers.add(c);
	}
	
	public void removeCursorObserver(CursorObserver c) {
		cursorObservers.remove(c);
	}
	
	
	public void addSelectionObserver(SelectionObserver c) {
		selectionObservers.add(c);
	}
	
	public void removeSelectionObserver(SelectionObserver c) {
		selectionObservers.remove(c);
	}
	
	private void notifySelectionObservers() {
		for (SelectionObserver c : new ArrayList<>(selectionObservers)) {
			c.selectionChanged(isSelected);
		}
	}
	
	public Iterator<String> allLines() {
		return new TextEditorIterator(0, lines.size());
	}
	
	public Iterator<String> linesRange(int index1, int index2) {
		if (index1 < 0 || index2 > lines.size())
			throw new IndexOutOfBoundsException("Indeks ne smije biti veci od broja linija");
		
		return new TextEditorIterator(index1, index2);
	}
	
	public List<String> getLines() {
		return lines;
	}
	
	public void setLines(List<String> lines) {
		this.lines = lines;
	}
	
	public LocationRange getSelectionRange() {
		//normalizeLocationRange(selectionRange);
		return selectionRange;
	}
	
	public void normalizeLocationRange(LocationRange r) {
		if (r.l2.y < r.l1.y || (r.l2.y == r.l1.y && r.l2.x < r.l1.x)) {
			Location pom = r.l2.copy();
			r.l2 = r.l1.copy();
			r.l1 = pom;
		}
	}
	
	public Location getCursorLocation() {
		return cursorLocation;
	}
	
	public void setCursorLocation(Location cursorLocation) {
		this.cursorLocation = cursorLocation.copy();
		notifyCursorObservers();
	}
	
	
	public void setSelectionStart(Location start) {
		selectionRange.l1 = start.copy();
		isSelected = true;
		
		notifyTextObservers();
		notifySelectionObservers();
	}

	public void setSelectionEnd(Location end) {
		selectionRange.l2 = end.copy();
		isSelected = true;
		
		notifyTextObservers();
		notifySelectionObservers();
	}
	
	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
		notifySelectionObservers();
	}
	
	private class TextEditorIterator implements Iterator<String> {
		
		private int end;
		private int currentIndex = 0;
		
		public TextEditorIterator(int index1, int index2) {
			this.currentIndex = index1;
			this.end = index2;
		}

		@Override
		public boolean hasNext() {
			return currentIndex < end;
		}

		@Override
		public String next() {
			return lines.get(currentIndex++);
		}
		
	}

}
