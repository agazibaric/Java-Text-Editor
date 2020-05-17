package hr.fer.zemris.ooup.lab3.texteditor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.BevelBorder;



public class MainFrame extends JFrame {


	private static final long serialVersionUID = 1L;
	
	private static final Point LOCATION_POINT = new Point(10, 10);

	private static final Dimension FRAME_SIZE = new Dimension(800, 600);

	private static final String FRAME_NAME = "Smart Text Editor";
	
	private TextEditor editor = new TextEditor();
	
	private JMenu fileMenu;

	private JMenu editMenu;
	
	private JMenu moveMenu;
	
	private JMenu pluginsMenu;
	
	private JPanel editorPanel = new JPanel();
	
	private JLabel statusBar;
	
	private JLabel cursorLabel;
	
	private JLabel nRowLabel;
	
	private List<Plugin> plugins = new ArrayList<>();
	
	public static void main(String[] args) {
		
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		}
		
		SwingUtilities.invokeLater(() -> {
			new MainFrame().setVisible(true);
		});
	}
	
	public MainFrame() {
		loadAllPlugins();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocation(LOCATION_POINT);
		setSize(FRAME_SIZE);
		setTitle(FRAME_NAME);
		initGUI();
	}
	
	private void loadAllPlugins() {
		try (Stream<Path> paths = Files.walk(Paths.get("./src/main/resources/"))) {
		    paths.filter(Files::isRegularFile)
		         .forEach(p -> loadPlugin(p));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unchecked"})
	private void loadPlugin(Path pluginPath) {
		try(JarFile jarFile = new JarFile(pluginPath.toString())) {
			
			Enumeration<JarEntry> e = jarFile.entries();
	
			URL[] urls = { new URL("jar:file:" + pluginPath.toString()+"!/") };
			URLClassLoader cl = URLClassLoader.newInstance(urls);
	
			while (e.hasMoreElements()) {
			    JarEntry je = e.nextElement();
			    if(je.isDirectory() || !je.getName().endsWith(".class")){
			        continue;
			    }
			    // -6 because of .class
			    String className = je.getName().substring(0,je.getName().length()-6);
			    className = className.replace('/', '.');
			    Class<Plugin> c = (Class<Plugin>) cl.loadClass(className);
			    
			    Constructor<?> ctr = c.getConstructor(JFrame.class);
			    Plugin plugin = (Plugin) ctr.newInstance(this);
			    plugins.add(plugin);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initGUI() {
		Container cp = this.getContentPane();
		cp.setLayout(new BorderLayout());
		
		editorPanel.setLayout(new BorderLayout());
		editorPanel.add(editor, BorderLayout.CENTER);
		cp.add(editorPanel, BorderLayout.CENTER);
		
		this.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				editor.keyTyped(e);
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				editor.keyReleased(e);
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				editor.keyPressed(e);
			}
		});
		
		initActions();
		createMenus();
		createToolbar();
		createStatusBar();
		createObservers();
		
		this.setFocusable(true);
		this.requestFocus();

	}
	
	private void createObservers() {
		
		SelectionObserver copyObs = new SelectionObserver() {
			
			@Override
			public void selectionChanged(boolean isSelected) {
				copyAction.setEnabled(isSelected);
				
			}
		};
		
		SelectionObserver cutObs = new SelectionObserver() {
			
			@Override
			public void selectionChanged(boolean isSelected) {
				cutAction.setEnabled(isSelected);
				
			}
		};
		
		ClipboardObserver pasteObs = new ClipboardObserver() {
			
			@Override
			public void updateClipboard(boolean isStackEmpty) {
				pasteAction.setEnabled(!isStackEmpty);
			}
		};
		
		ClipboardObserver pasteAndTakeObs = new ClipboardObserver() {
			
			@Override
			public void updateClipboard(boolean isStackEmpty) {
				pasteAndTakeAction.setEnabled(!isStackEmpty);
			}
		};
		
		UndoObserver undoObs = new UndoObserver() {
			
			@Override
			public void undoStackStatus(boolean isStackEmpty) {
				undoAction.setEnabled(!isStackEmpty);
			}
			
			@Override
			public void redoStackStatus(boolean isStackEmpty) {
				redoAction.setEnabled(!isStackEmpty);
				
			}
		};
		
		editor.clipboard.addObserver(pasteObs);
		editor.clipboard.addObserver(pasteAndTakeObs);
		editor.model.addSelectionObserver(cutObs);
		editor.model.addSelectionObserver(copyObs);
		editor.model.undoManager.addObserver(undoObs);
		
	}
	
	private void initActions() {
		openAction.putValue(Action.NAME, "Open");
		saveAction.putValue(Action.NAME, "Save");
		exitAction.putValue(Action.NAME, "Exit");
		
		undoAction.putValue(Action.NAME, "Undo");
		redoAction.putValue(Action.NAME, "Redo");
		cutAction.putValue(Action.NAME, "Cut");
		copyAction.putValue(Action.NAME, "Copy");
		pasteAction.putValue(Action.NAME, "Paste");
		pasteAndTakeAction.putValue(Action.NAME, "Paste and take");
		deleteSelectionAction.putValue(Action.NAME, "Delete selection");
		clearDocumentAction.putValue(Action.NAME, "Clear document");
		
		cursorStartAction.putValue(Action.NAME, "Cursor to document start");
		cursorEndAction.putValue(Action.NAME, "Cursor to document end");
		
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
		cutAction.setEnabled(false);
		copyAction.setEnabled(false);
		pasteAction.setEnabled(false);
		pasteAndTakeAction.setEnabled(false);
		deleteSelectionAction.setEnabled(false);
		
		
	}
	
	private void createMenus() {
		JMenuBar menuBar = new JMenuBar();
		
		fileMenu = new JMenu("File");
		fileMenu.add(openAction);
		fileMenu.add(saveAction);
		fileMenu.addSeparator();
		fileMenu.add(exitAction);
		
		editMenu = new JMenu("Edit");
		editMenu.add(undoAction);
		editMenu.add(redoAction);
		editMenu.add(cutAction);
		editMenu.add(copyAction);
		editMenu.add(pasteAction);
		editMenu.add(pasteAndTakeAction);
		editMenu.add(deleteSelectionAction);
		editMenu.add(clearDocumentAction);
		
		moveMenu = new JMenu("Move");
		moveMenu.add(cursorStartAction);
		moveMenu.add(cursorEndAction);
		
		pluginsMenu = new JMenu("Plugins");
		for (Plugin p : plugins) {
			Action pluginAction = new AbstractAction() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					p.execute(editor.model, editor.model.undoManager, editor.clipboard);
				}
				
			};
			pluginAction.putValue(Action.NAME, p.getName());
			pluginAction.putValue(Action.SHORT_DESCRIPTION, p.getDescription());
			pluginsMenu.add(pluginAction);
		}
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(moveMenu);
		menuBar.add(pluginsMenu);
		
		this.setJMenuBar(menuBar);
	}
	
	private void createToolbar() {
		
		JToolBar toolBar = new JToolBar();
		
		toolBar.add(new JButton(undoAction));
		toolBar.add(new JButton(redoAction));
		toolBar.addSeparator();
		toolBar.add(new JButton(cutAction));
		toolBar.add(new JButton(copyAction));
		toolBar.add(new JButton(pasteAction));
		
		this.getContentPane().add(toolBar, BorderLayout.PAGE_START);
		
	}
	
	private void createStatusBar() {
		
		statusBar = new JLabel();
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
		statusBar.setPreferredSize(new Dimension(getWidth(), 20));
		
		cursorLabel = new JLabel("Cursor location: 0 ");
		nRowLabel = new JLabel("Number of rows: 0 ");
		
		statusBar.add(cursorLabel);
		statusBar.add(new JLabel(" |  "));
		statusBar.add(nRowLabel);

		
		statusBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		editorPanel.add(statusBar, BorderLayout.SOUTH);
		
		editor.model.addTextObserver(new TextObserver() {
			
			@Override
			public void updateText() {
				nRowLabel.setText(String.format("Number of rows: %d ", editor.model.getLines().size()));
			}
		});
		
		editor.model.addCursorObserver(new CursorObserver() {
			
			@Override
			public void updateCursorLocation(Location loc) {
				cursorLabel.setText(String.format("Cursor location: (%d, %d) ", loc.x, loc.y));
			}
		});
		
	}
	
	private final Action openAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Open document");
			if (chooser.showOpenDialog(MainFrame.this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			
			File fileName = chooser.getSelectedFile();
			Path filePath = fileName.toPath();
			if (!Files.isReadable(filePath)) {
				JOptionPane.showMessageDialog(MainFrame.this, "File " + fileName.getAbsolutePath() + " does not exit",
						"Error",JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String textContent;
			try {
				byte[] content = Files.readAllBytes(filePath);
				textContent = new String(content, "UTF-8");
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(MainFrame.this, "Pogreška prilikom učitavanja datoteke " + filePath,
						"Pogreška", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			editor.clearAll();
			editor.addNewText(textContent);
		}

	};
	
	private final Action saveAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Save document");

			if (chooser.showSaveDialog(MainFrame.this) != JFileChooser.APPROVE_OPTION) {
				JOptionPane.showMessageDialog(MainFrame.this, "File is not saved", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			Path filePath = chooser.getSelectedFile().toPath();
			
			byte[] bytes = editor.getContent().getBytes();
			try {
				Files.write(filePath, bytes);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(MainFrame.this, "Pogreška prilikom učitavanja datoteke " + filePath,
						"Pogreška", JOptionPane.ERROR_MESSAGE);
			}
		}

	};
	
	private final Action exitAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			exitProgram();
		}

	};
	
	private final Action undoAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			editor.undo();
			MainFrame.this.requestFocus();
		}

	};
	
	private final Action redoAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			editor.redo();
			MainFrame.this.requestFocus();
		}

	};
	
	private final Action cutAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			editor.cut();
			MainFrame.this.requestFocus();
		}

	};
	
	private final Action copyAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			editor.copy();
			MainFrame.this.requestFocus();
		}

	};
	
	private final Action pasteAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			editor.paste();
			MainFrame.this.requestFocus();
		}

	};
	
	private final Action pasteAndTakeAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			editor.pasteAndTake();
			MainFrame.this.requestFocus();
		}

	};
	
	private final Action deleteSelectionAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			editor.deleteSelection();
			MainFrame.this.requestFocus();
		}

	};
	
	private final Action clearDocumentAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			editor.clearAll();
			MainFrame.this.requestFocus();
		}

	};
	
	private final Action cursorStartAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			editor.moveCursorToStart();
			MainFrame.this.requestFocus();
		}

	};
	
	private final Action cursorEndAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			editor.moveCursorToEnd();
			MainFrame.this.requestFocus();
		}

	};
	
	private void exitProgram() {
		this.dispose();
	}

}
