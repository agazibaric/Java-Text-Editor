package hr.fer.zemris.ooup.lab3.texteditor.plugins;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.text.WordUtils;

import hr.fer.zemris.ooup.lab3.texteditor.ClipboardStack;
import hr.fer.zemris.ooup.lab3.texteditor.Plugin;
import hr.fer.zemris.ooup.lab3.texteditor.TextEditorModel;
import hr.fer.zemris.ooup.lab3.texteditor.UndoManager;

public class CapitalLetterPlugin implements Plugin {
	
	private String name = "Capital letter";
	private String description = "Shows number of rows, words and letters";
	private JFrame mainFrame;
	
	public CapitalLetterPlugin(JFrame mainFrame) {
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
		
		capitalize(model.getLines());
		model.forceNotify();
		String info = "Every first letter in word became a capital letter!";
		JOptionPane.showMessageDialog(mainFrame, info, "Statistical informations",
				JOptionPane.INFORMATION_MESSAGE);
		
	}
	
	public void capitalize(List<String> lines) {
		
		for (int i = 0, n = lines.size(); i < n; i++) {
			String line = lines.get(i);
			String capitalizedLine = WordUtils.capitalize(line);
			lines.set(i, capitalizedLine);
		}
		
	}

}
