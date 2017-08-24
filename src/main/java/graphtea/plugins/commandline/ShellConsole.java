// GraphTea Project: http://github.com/graphtheorysoftware/GraphTea
// Copyright (C) 2012 Graph Theory Software Foundation: http://GraphTheorySoftware.com
// Copyright (C) 2008 Mathematical Science Department of Sharif University of Technology
// Distributed under the terms of the GNU General Public License (GPL): http://www.gnu.org/licenses/

package graphtea.plugins.commandline;

import bsh.ConsoleInterface;
import bsh.util.GUIConsoleInterface;
import bsh.util.NameCompletion;
import graphtea.platform.core.exception.ExceptionHandler;
import graphtea.plugins.commandline.parsers.DefaultParser;
import graphtea.plugins.commandline.util.Utils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Vector;

public class ShellConsole extends JScrollPane
        implements GUIConsoleInterface, Runnable, KeyListener,
        MouseListener, ActionListener, PropertyChangeListener {
    private final static String CUT = "Cut";
    private final static String COPY = "Copy";
    private final static String PASTE = "Paste";

    private OutputStream outPipe;
    private InputStream inPipe;
    private InputStream in;
    private PrintStream out;
    public Shell shell;

    public InputStream getInputStream() {
        return in;
    }

    public Reader getIn() {
        return new InputStreamReader(in);
    }

    public PrintStream getOut() {
        return out;
    }

    public PrintStream getErr() {
        return out;
    }

    private int cmdStart = 0;
    private Vector history = new Vector();
    private String startedLine;
    private int histLine = 0;

    private JPopupMenu menu;
    private JTextPane text;
    private DefaultStyledDocument doc;

    NameCompletion nameCompletion;
    final int SHOW_AMBIG_MAX = 15;

    // hack to prevent key repeat for some reason?
    private boolean gotUp = true;

    public ShellConsole() {
        this(null, null);
    }

    public ConsoleInterface console_interface;
    public boolean is_interface = false;

    public void set(ConsoleInterface ci) {
        console_interface = ci;
        is_interface = true;
    }

    public ShellConsole(InputStream cin, OutputStream cout) {
        super();
        //  parser.put(dp.getName(), dp);
        is_interface = false;

        // Special TextPane which catches for cut and paste, both L&F keys and
        // programmatic	behaviour
        text = new JTextPane(doc = new DefaultStyledDocument()) {
            public void cut() {
                if (text.getCaretPosition() < cmdStart) {
                    super.copy();
                } else {
                    super.cut();
                }
            }

            public void paste() {
                forceCaretMoveToEnd();
                super.paste();
            }
        };


        Font font = new Font("Monospaced", Font.PLAIN, 12);

        text.setText("");
        text.setFont(font);

        text.setMargin(new Insets(7, 5, 7, 5));
        text.addKeyListener(this);
        setViewportView(text);

        // create popup	menu
        menu = new JPopupMenu("MyConsole	Menu");
        menu.add(new JMenuItem(CUT)).addActionListener(this);
        menu.add(new JMenuItem(COPY)).addActionListener(this);
        menu.add(new JMenuItem(PASTE)).addActionListener(this);

        text.addMouseListener(this);

        // make	sure popup menu	follows	Look & Feel
        UIManager.addPropertyChangeListener(this);

        outPipe = cout;
        if (outPipe == null) {
            outPipe = new PipedOutputStream();
            try {
                in = new PipedInputStream((PipedOutputStream) outPipe);
            } catch (IOException e) {
                print("Console internal	error (1)...", Color.red);
            }
        }

        inPipe = cin;
        if (inPipe == null) {
            PipedOutputStream pout = new PipedOutputStream();
            out = new PrintStream(pout);
            try {
                inPipe = new BlockingPipedInputStream(pout);
            } catch (IOException e) {
                print("Console internal error: " + e);
            }
        }
        // Start the inpipe watcher
        new Thread(this).start();

        requestFocus();
    }

    public void requestFocus() {
        super.requestFocus();
        text.requestFocus();
    }

    public void keyPressed(KeyEvent e) {
        type(e);
        gotUp = false;
    }

    public void keyTyped(KeyEvent e) {
        type(e);
    }

    public void keyReleased(KeyEvent e) {
        gotUp = true;
        type(e);
    }

    private synchronized void type(KeyEvent e) {
        switch (e.getKeyCode()) {
            case(KeyEvent.VK_ENTER):
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (gotUp) {
                        enter();
                        resetCommandStart();
                        text.setCaretPosition(cmdStart);
                    }
                }
                e.consume();
                text.repaint();
                break;

            case(KeyEvent.VK_UP):
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    historyUp();
                }
                e.consume();
                break;

            case(KeyEvent.VK_DOWN):
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    historyDown();
                }
                e.consume();
                break;

            case(KeyEvent.VK_LEFT):
            case(KeyEvent.VK_BACK_SPACE):
            case(KeyEvent.VK_DELETE):
                if (text.getCaretPosition() <= cmdStart) {
                    // This doesn't work for backspace.
                    // See default case for workaround
                    e.consume();
                }
                break;

            case(KeyEvent.VK_RIGHT):
                forceCaretMoveToStart();
                break;

            case(KeyEvent.VK_HOME):
                text.setCaretPosition(cmdStart);
                e.consume();
                break;

            case(KeyEvent.VK_U):    // clear line
                if ((e.getModifiers() & InputEvent.CTRL_MASK) > 0) {
                    replaceRange("", cmdStart, textLength());
                    histLine = 0;
                    e.consume();
                }
                break;

            case(KeyEvent.VK_ALT):
            case(KeyEvent.VK_CAPS_LOCK):
            case(KeyEvent.VK_CONTROL):
            case(KeyEvent.VK_META):
            case(KeyEvent.VK_SHIFT):
            case(KeyEvent.VK_PRINTSCREEN):
            case(KeyEvent.VK_SCROLL_LOCK):
            case(KeyEvent.VK_PAUSE):
            case(KeyEvent.VK_INSERT):
            case(KeyEvent.VK_F1):
            case(KeyEvent.VK_F2):
            case(KeyEvent.VK_F3):
            case(KeyEvent.VK_F4):
            case(KeyEvent.VK_F5):
            case(KeyEvent.VK_F6):
            case(KeyEvent.VK_F7):
            case(KeyEvent.VK_F8):
            case(KeyEvent.VK_F9):
            case(KeyEvent.VK_F10):
            case(KeyEvent.VK_F11):
            case(KeyEvent.VK_F12):
            case(KeyEvent.VK_ESCAPE):

                // only	modifier pressed
                break;

                // Control-C
            case(KeyEvent.VK_C):
                if (text.getSelectedText() == null) {
                    if (((e.getModifiers() & InputEvent.CTRL_MASK) > 0)
                            && (e.getID() == KeyEvent.KEY_PRESSED)) {
                        append("^C");
                    }
                    e.consume();
                }
                break;

            case(KeyEvent.VK_TAB):
                if (e.getID() == KeyEvent.KEY_RELEASED) {
                    String part = text.getText().substring(cmdStart);
                    doCommandCompletion(part);
                }
                e.consume();
                break;

            default:
                if (
                        (e.getModifiers() &
                                (InputEvent.CTRL_MASK
                                        | InputEvent.ALT_MASK | InputEvent.META_MASK)) == 0) {
                    // plain character
                    forceCaretMoveToEnd();
                }

                /*
                        The getKeyCode function always returns VK_UNDEFINED for
                        keyTyped events, so backspace is not fully consumed.
                    */
                if (e.paramString().contains("Backspace")) {
                    if (text.getCaretPosition() <= cmdStart) {
                        e.consume();
                        break;
                    }
                }

                break;
        }
    }

    public void clear() {
        text.setText("");
        text.repaint();
    }

    private void doCommandCompletion(String part) {
        String bk = part;
        if (nameCompletion == null)
            return;

        int i = part.length() - 1;

        // Character.isJavaIdentifierPart()  How convenient for us!!
        while (
                i >= 0 &&
                        (Character.isJavaIdentifierPart(part.charAt(i))
                                || part.charAt(i) == '.' || part.charAt(i) == '('
                                || part.charAt(i) == ')' || part.charAt(i) == '[' || part.charAt(i) == ']')
                )
            i--;


        part = part.substring(i + 1);
        if (part.length() < 1)  // reasonable completion length
            return;


        int index = Math.max(bk.lastIndexOf("bsh % "), bk.lastIndexOf(">>  "));
        if (index <= 0) index = 0;
        else index = Math.max(bk.lastIndexOf("bsh % ") + 6, bk.lastIndexOf(">>  ") + 4);
        String ret = bk.substring(index, i + 1);
        ret.trim();
        // no completion
        String[] complete = nameCompletion.completeName(part);
        if (complete.length == 0) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }

        // Found one completion (possibly what we already have)
        if (complete.length == 1 && !complete.equals(part)) {
            if (part.endsWith("(")) {
                if (complete[0].equals(part + ");")) {
                    String append = complete[0].substring(part.length());
                    append(append);
                    return;
                }

            } else if (!part.startsWith("_")) {
                String append = complete[0].substring(part.length()) + "(";
                append(append);
                return;
            } else {
                if (!complete[0].startsWith("_")) {
                    text.select(textLength() - part.length(), textLength());
                    text.replaceSelection(complete[0]);
                    return;
                }
            }
        }

        // Found ambiguous, show (some of) them
        String line = text.getText();
//        String command = line.substring(cmdStart);
        // Find prompt
        for (i = cmdStart; line.charAt(i) != '\n' && i > 0; i--) ;
        String prompt = ">>  ";//line.substring( i+1, cmdStart );

        // Show ambiguous
        StringBuffer sb = new StringBuffer("\n");
        for (i = 0; i < complete.length && i < SHOW_AMBIG_MAX; i++)
            sb.append(complete[i] + "\n");
        if (i == SHOW_AMBIG_MAX)
            sb.append("...\n");

        print(sb, Color.blue);
        print(prompt); // print resets command start
        if (complete.length != 1)
            append(ret + Utils.getMaximumSimilarities(complete));
        else append(ret + part);
    }

    private void resetCommandStart() {
        cmdStart = textLength();
    }

    private void append(String string) {

        int slen = textLength();
        text.select(slen, slen);
        text.replaceSelection(string);
    }

    private String replaceRange(Object s, int start, int end) {
        String st = s.toString();
        text.select(start, end);
        text.replaceSelection(st);
        //text.repaint();
        return st;
    }

    private void forceCaretMoveToEnd() {
        if (text.getCaretPosition() < cmdStart) {
            // move caret first!
            text.setCaretPosition(textLength());
        }
        text.repaint();
    }

    private void forceCaretMoveToStart() {
        if (text.getCaretPosition() < cmdStart) {
            // move caret first!
        }
        text.repaint();
    }


    private void enter() {
        String s = getCmd();

        if (s.length() == 0)    // special hack	for empty return!
            s = ";\n";
        else {
            history.addElement(s);
            s = s + "\n";
        }

        append("\n");
        histLine = 0;
        acceptLine(s);
        text.repaint();
    }

    private String getCmd() {
        String s = "";
        try {
            s = text.getText(cmdStart, textLength() - cmdStart);
        } catch (BadLocationException e) {
            // should not happen
            System.out.println("Internal MyConsole Error: " + e);
        }
        return s;
    }

    private void historyUp() {
        if (history.size() == 0)
            return;
        if (histLine == 0)  // save current line
            startedLine = getCmd();
        if (histLine < history.size()) {
            histLine++;
            showHistoryLine();
        }
    }

    private void historyDown() {
        if (histLine == 0)
            return;

        histLine--;
        showHistoryLine();
    }

    private void showHistoryLine() {
        String showline;
        if (histLine == 0)
            showline = startedLine;
        else
            showline = (String) history.elementAt(history.size() - histLine);

        replaceRange(showline, cmdStart, textLength());
        text.setCaretPosition(textLength());
        text.repaint();
    }

    String ZEROS = "000";

    //boolean is_equal = ;

    //HashMap<String, ExtParser> parser = new HashMap<String, ExtParser>();
    //ExtParser defaultExtParser;

    boolean is_accepted = true;
    String me_buffered = "";

    private void acceptLine(String line) {
        if (line.contains(";"))
            if (!is_accepted) {
                line = me_buffered + line;
                me_buffered = "";
                is_accepted = true;
            } else me_buffered = "";

        else {
            me_buffered += line;
            is_accepted = false;
            return;
        }
        line = new DefaultParser(shell).parse(line);

        // Patch to handle Unicode characters
        // Submitted by Daniel Leuck
        StringBuffer buf = new StringBuffer();
        int lineLength = line.length();
        for (int i = 0; i < lineLength; i++) {
            String val = Integer.toString(line.charAt(i), 16);
            val = ZEROS.substring(0, 4 - val.length()) + val;
            buf.append("\\u" + val);
        }
        line = buf.toString();
        // End unicode patch

        if (outPipe == null)
            print("Console internal	error: cannot output ...", Color.red);
        else
            try {
                outPipe.write(line.getBytes());
                outPipe.flush();
            } catch (IOException e) {
                outPipe = null;
                throw new RuntimeException("Console pipe broken...");
            }
        text.repaint();
    }

    public void println(Object o) {
        if (is_interface) {
            console_interface.println(o);
            return;
        }

        print(String.valueOf(o) + "\n");
        text.repaint();
    }

    public void print(final Object o) {
        if (is_interface) {
            console_interface.print(o);
            return;
        }
        invokeAndWait(new Runnable() {
            public void run() {
                append(String.valueOf(o));
                resetCommandStart();
                text.setCaretPosition(cmdStart);
            }
        });
    }

    public Color getResultColor() {
        return Color.blue;
    }

    public void printResult(Object s) {
        print(s, getResultColor());
    }

    public void printlnResult(Object s) {
        println(s, getResultColor());
    }

    /**
     * Prints "\\n" (i.e. newline)
     */
    public void println() {
        print("\n");
        text.repaint();
    }

    public void error(Object o) {
        if (is_interface) {
            console_interface.error(o);
            return;
        }

        print("err: " + o, Color.red);
        println();
    }

    public void println(Icon icon) {
        print(icon);
        println();
        text.repaint();
    }

    public void print(final Icon icon) {
        if (icon == null)
            return;

        invokeAndWait(new Runnable() {
            public void run() {
                text.insertIcon(icon);
                resetCommandStart();
                text.setCaretPosition(cmdStart);
            }
        });
    }

    public void print(Object s, Font font) {
        if (is_interface) {
            print(s);
            return;
        }
        print(s, font, null);
    }

    public void println(Object s, Color color) {
        if (is_interface) {
            println(s);
            return;
        }
        print(s, null, color);
        println();
    }

    public void print(Object s, Color color) {
        if (is_interface) {
            print(s);
            return;
        }
        print(s, null, color);
    }

    public void print(final Object o, final Font font, final Color color) {
        invokeAndWait(new Runnable() {
            public void run() {
                AttributeSet old = getStyle();
                setStyle(font, color);
                append(String.valueOf(o));
                resetCommandStart();
                text.setCaretPosition(cmdStart);
                setStyle(old, true);
            }
        });
    }

    public void print(
            Object s,
            String fontFamilyName,
            int size,
            Color color
    ) {

        print(s, fontFamilyName, size, color, false, false, false);
    }

    public void print(
            final Object o,
            final String fontFamilyName,
            final int size,
            final Color color,
            final boolean bold,
            final boolean italic,
            final boolean underline
    ) {
        invokeAndWait(new Runnable() {
            public void run() {
                AttributeSet old = getStyle();
                setStyle(fontFamilyName, size, color, bold, italic, underline);
                append(String.valueOf(o));
                resetCommandStart();
                text.setCaretPosition(cmdStart);
                setStyle(old, true);
            }
        });
    }

    private AttributeSet setStyle(Font font) {
        return setStyle(font, null);
    }

    private AttributeSet setStyle(Color color) {
        return setStyle(null, color);
    }

    private AttributeSet setStyle(Font font, Color color) {
        if (font != null)
            return setStyle(font.getFamily(), font.getSize(), color,
                    font.isBold(), font.isItalic(),
                    StyleConstants.isUnderline(getStyle()));
        else
            return setStyle(null, -1, color);
    }

    private AttributeSet setStyle(
            String fontFamilyName, int size, Color color) {
        MutableAttributeSet attr = new SimpleAttributeSet();
        if (color != null)
            StyleConstants.setForeground(attr, color);
        if (fontFamilyName != null)
            StyleConstants.setFontFamily(attr, fontFamilyName);
        if (size != -1)
            StyleConstants.setFontSize(attr, size);

        setStyle(attr);

        return getStyle();
    }

    private AttributeSet setStyle(
            String fontFamilyName,
            int size,
            Color color,
            boolean bold,
            boolean italic,
            boolean underline
    ) {
        MutableAttributeSet attr = new SimpleAttributeSet();
        if (color != null)
            StyleConstants.setForeground(attr, color);
        if (fontFamilyName != null)
            StyleConstants.setFontFamily(attr, fontFamilyName);
        if (size != -1)
            StyleConstants.setFontSize(attr, size);
        StyleConstants.setBold(attr, bold);
        StyleConstants.setItalic(attr, italic);
        StyleConstants.setUnderline(attr, underline);

        setStyle(attr);

        return getStyle();
    }

    private void setStyle(AttributeSet attributes) {
        setStyle(attributes, false);
    }

    private void setStyle(AttributeSet attributes, boolean overWrite) {
        text.setCharacterAttributes(attributes, overWrite);
    }

    private AttributeSet getStyle() {
        return text.getCharacterAttributes();
    }

    public void setFont(Font font) {
        super.setFont(font);

        if (text != null)
            text.setFont(font);
    }

    private void inPipeWatcher() throws IOException {
        byte[] ba = new byte[256]; //	arbitrary blocking factor
        int read;
        while ((read = inPipe.read(ba)) != -1) {
            print(new String(ba, 0, read));
            //text.repaint();
        }

        println("Console: Input	closed...");
    }

    public void run() {
        try {
            inPipeWatcher();
        } catch (IOException e) {
            print("Console: I/O Error: " + e + "\n", Color.red);
        }
    }

    public String toString() {
        return "BeanShell console";
    }

    // MouseListener Interface
    public void mouseClicked(MouseEvent event) {
    }

    public void mousePressed(MouseEvent event) {
        if (event.isPopupTrigger()) {
            menu.show(
                    (Component) event.getSource(), event.getX(), event.getY());
        }
    }

    public void mouseReleased(MouseEvent event) {
        if (event.isPopupTrigger()) {
            menu.show((Component) event.getSource(), event.getX(),
                    event.getY());
        }
        text.repaint();
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

    // property	change
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("lookAndFeel")) {
            SwingUtilities.updateComponentTreeUI(menu);
        }
    }

    // handle cut, copy	and paste
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        switch (cmd) {
            case CUT:
                text.cut();
                break;
            case COPY:
                text.copy();
                break;
            case PASTE:
                text.paste();
                break;
        }
    }

    /**
     * If not in the event thread run via SwingUtilities.invokeAndWait()
     */
    private void invokeAndWait(Runnable run) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(run);
            } catch (Exception e) {
                // shouldn't happen
                ExceptionHandler.catchException(e);
            }
        } else {
            run.run();
        }
    }

    /**
     * The overridden read method in this class will not throw "Broken pipe"
     * IOExceptions;  It will simply wait for new writers and data.
     * This is used by the MyConsole internal read thread to allow writers
     * in different (and in particular ephemeral) threads to write to the pipe.
     * <p/>
     * It also checks a little more frequently than the original read().
     * <p/>
     * Warning: read() will not even error on a read to an explicitly closed
     * pipe (override closed to for that).
     */
    public static class BlockingPipedInputStream extends PipedInputStream {
        boolean closed;

        public BlockingPipedInputStream(PipedOutputStream pout)
                throws IOException {
            super(pout);
        }

        public synchronized int read() throws IOException {
            if (closed)
                throw new IOException("stream closed");

            while (super.in < 0) {    // While no data */
                notifyAll();    // Notify any writers to wake up
                try {
                    wait(750);
                } catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
            }
            // This is what the superclass does.
            int ret = buffer[super.out++] & 0xFF;
            if (super.out >= buffer.length)
                super.out = 0;
            if (super.in == super.out)
                super.in = -1;  /* now empty */
            return ret;
        }

        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    public void setNameCompletion(NameCompletion nc) {
        this.nameCompletion = nc;
    }

    public void setWaitFeedback(boolean on) {
        if (on)
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        else
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private int textLength() {
        return text.getDocument().getLength();
    }

}


