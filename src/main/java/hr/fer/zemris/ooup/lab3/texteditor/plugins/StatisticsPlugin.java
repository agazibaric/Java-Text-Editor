package hr.fer.zemris.ooup.lab3.texteditor.plugins;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import hr.fer.zemris.ooup.lab3.texteditor.ClipboardStack;
import hr.fer.zemris.ooup.lab3.texteditor.Plugin;
import hr.fer.zemris.ooup.lab3.texteditor.TextEditorModel;
import hr.fer.zemris.ooup.lab3.texteditor.UndoManager;

public class StatisticsPlugin implements Plugin {
	
	private String name = "Statistics";
	private String description = "Shows number of rows, words and letters";
	private JFrame mainFrame;
	
	public StatisticsPlugin(JFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack) {
		
		List<String> lines = model.getLines();
		
		int numOfChars = getNumOfChars(lines);
		int numOfLines = lines.size();
		int numOfWords = getNumOfWords(lines);
		
		String info =  String.format(
				"- Number of characters: %d%n" + 
				"- Number of lines: %d%n" + 
				"- Number of words: %d",
				numOfChars, numOfLines, numOfWords);
		
		JOptionPane.showMessageDialog(mainFrame, info, "Statistical informations",
				JOptionPane.INFORMATION_MESSAGE);
		
	}
	
	
	public int getNumOfChars(List<String> lines) {
		int count = 0;
		for (String l : lines) {
			count += l.length();
		}
		return count;
	}
	
	public int getNumOfWords(List<String> lines) {
		int count = 0;
		
		for (String l : lines) {
			String trim = l.trim();
			if (trim.isEmpty())
				continue;
			count += trim.split("\\s+").length;
		}
		
		return count;
	}
	

}
