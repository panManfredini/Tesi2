package de.feynarts;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;


public class TeXPanel extends JPanel {
  private JMenu menu;

  private JTextArea textArea;
  private ExportImportReceiver receiver;
  private Addables actions;

  public TeXPanel(ExportImportReceiver receiver) {
    super(new BorderLayout());

    textArea = new JTextArea(10,30);
    this.receiver = receiver;

    add(new JScrollPane(textArea), BorderLayout.CENTER);

    initActions();

    JToolBar toolBar = new JToolBar();
    actions.addTo(toolBar);

    menu = new JMenu("TeX");
    menu.setMnemonic(KeyEvent.VK_X);
    actions.addTo(menu);

    add(toolBar, BorderLayout.NORTH);
  }

  private void initActions() {
    actions = new Addables();
    Separator separator = Separator.instance();

    actions.add(new ClearAction());
    actions.add(separator);

    actions.add(new PasteAction());
    actions.add(new CopyAction());
    actions.add(separator);

    actions.add(new ImportAction());
    actions.add(new ExportAction());
  }

  public JMenu getMenu() {
    return menu;
  }

  public class ImportAction extends AbstractAction {
    public ImportAction() {
      super("To TeX", Editor.newImageIcon("Import"));

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_X, 
          ActionEvent.CTRL_MASK +
          ActionEvent.SHIFT_MASK
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_X)
      );
    }

    public void actionPerformed(ActionEvent e) {
      textArea.setText(receiver.getText());
    }
  }

  public class ExportAction extends AbstractAction {
    public ExportAction() {
      super("To Topology", Editor.newImageIcon("Export"));

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_X, 
          ActionEvent.CTRL_MASK
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_T)
      );
    }

    public void actionPerformed(ActionEvent e) {
      receiver.setText(textArea.getText());
    }
  }

  public class ClearAction extends AbstractAction {
    public ClearAction() {
      super("Clear", Editor.newImageIcon("New"));

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_DELETE, 
          ActionEvent.CTRL_MASK
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_L)
      );
    }

    public void actionPerformed(ActionEvent e) {
      textArea.setText("");
    }
  }

  public class CopyAction extends AbstractAction {
    public CopyAction() {
      super("Copy", Editor.newImageIcon("Copy"));

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_C, 
          ActionEvent.CTRL_MASK
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_C)
      );
    }

    public void actionPerformed(ActionEvent e) {
      String s = textArea.getText();
      StringSelection contents = new StringSelection(s);
      getToolkit().getSystemClipboard().setContents(contents, null);
    }
  }

  public class PasteAction extends AbstractAction {
    public PasteAction() {
      super("Paste", Editor.newImageIcon("Paste"));

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_P, 
          ActionEvent.CTRL_MASK
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_V)
      );
    }

    public void actionPerformed(ActionEvent e) {
      Transferable content = getToolkit().getSystemClipboard().getContents(this);
      try {
        String s = (String)content.getTransferData(DataFlavor.stringFlavor);
        textArea.setText(s);
      }
      catch( Throwable exception ) {}
    }
  }
}

