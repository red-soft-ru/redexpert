/*
 * PrintPreviewer.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.print;

import org.executequery.ActiveComponent;
import org.executequery.GUIUtilities;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.layouts.XYConstraints;
import org.underworldlabs.swing.layouts.XYLayout;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/**
 * <p>Print preview panel.
 *
 * @author Takis Diakoumis
 */
public class PrintPreviewer extends AbstractBaseDialog
        implements ActionListener,
        ActiveComponent {

    /**
     * The title of this panel
     */
    public static final String TITLE = Bundles.get("Print_Preview");

    /**
     * The icon for this panel
     */
    public static final String FRAME_ICON = "PrintPreview16.svg";

    /**
     * The scale combo box
     */
    private JComboBox scaleCombo;

    /**
     * The page width
     */
    private int m_wPage;

    /**
     * The page height
     */
    private int m_hPage;

    /**
     * The page format for this preview
     */
    private PageFormat format;

    private PreviewContainer previewContainer;

    private PrintingSupport printingSupport;

    private final String printJobName;

    private final Printable printable;

    public PrintPreviewer(Printable printable, String printJobName) {

        super(GUIUtilities.getParentFrame(), TITLE, true);

        this.printable = printable;
        this.printJobName = printJobName;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * <p>Initializes the state of this instance.
     */
    private void jbInit() throws Exception {

        printingSupport = new PrintingSupport();

        format = printingSupport.getPageFormat();

        if (format.getHeight() == 0 || format.getWidth() == 0) {

            Log.error("Unable to determine default page size");

            return;
        }


        JPanel base = new JPanel(new GridBagLayout());
        base.setPreferredSize(new Dimension(380, 500));
        previewContainer = new PreviewContainer();
        
/*
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        generatePreviewImage();
      }
    });
 */



        RolloverButton printButton = new RolloverButton(bundledString("Print"), null, 28, 55);
        RolloverButton setupButton = new RolloverButton(bundledString("Page_Setup"), null, 28, 85);
        RolloverButton closeButton = new RolloverButton(bundledString("Close"), null, 28, 55);

        JScrollPane scroller = new JScrollPane(previewContainer);

        printButton.setActionCommand("Print");
        setupButton.setActionCommand("Page Setup");
        closeButton.setActionCommand("Close");


        printButton.setMnemonic('P');
        setupButton.setMnemonic('S');
        closeButton.setMnemonic('C');

        String[] scales = {"10 %", "25 %", "50 %", "75 %", "100 %"};
        scaleCombo = WidgetFactory.createComboBox("scaleCombo", scales);
        scaleCombo.setActionCommand("scales");
        scaleCombo.setPreferredSize(new Dimension(70, 28));
        scaleCombo.setSelectedIndex(2);

        closeButton.addActionListener(this);
        setupButton.addActionListener(this);
        printButton.addActionListener(this);
        scaleCombo.addActionListener(this);

        JPanel buttonPanel = new JPanel(new XYLayout());
        XYConstraints xyc = new XYConstraints(0, 0, -1, -1);
        buttonPanel.add(printButton, xyc);
        xyc.setConstraints(60, 0, -1, -1);
        buttonPanel.add(scaleCombo, xyc);
        xyc.setConstraints(135, 0, -1, -1);
        buttonPanel.add(setupButton, xyc);
        xyc.setConstraints(225, 0, -1, -1);
        buttonPanel.add(closeButton, xyc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 0, 7);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        base.add(buttonPanel, gbc);
        gbc.insets.bottom = 7;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        base.add(scroller, gbc);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                generatePreviewImage();
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(base, BorderLayout.CENTER);

        display();
    }

    private void display() {
        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        setVisible(true);
    }

    public void dispose() {
        cleanup();
        GUIUtilities.scheduleGC();
        super.dispose();
    }


    public void cleanup() {
        format = null;
        previewContainer = null;
    }

    /**
     * <p>Performs the button actions.
     *
     * @param the event object
     */
    public void actionPerformed(ActionEvent e) {
        final String command = e.getActionCommand();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (command.equals("Close")) {
                    dispose();
                } else if (command.equals("Page Setup")) {
                    showPageSetup();
                } else if (command.equals("Print")) {
                    doPrint();
                } else if (command.equals("scales")) {
                    changeScale();
                }
            }

        });

    }

    private void changeScale() {
        Thread runner = new Thread() {
            public void run() {
                String str = scaleCombo.getSelectedItem().toString();

                if (str.endsWith("%"))
                    str = str.substring(0, str.length() - 1);

                str = str.trim();
                int scale = 0;

                try {
                    scale = Integer.parseInt(str);
                } catch (NumberFormatException ex) {
                }

                int w = (int) (m_wPage * scale / 100);
                int h = (int) (m_hPage * scale / 100);

                Component[] comps = previewContainer.getComponents();
                for (int k = 0; k < comps.length; k++) {

                    if (!(comps[k] instanceof PagePreview))
                        continue;

                    PagePreview pp = (PagePreview) comps[k];
                    pp.setScaledSize(w, h);
                }

                previewContainer.doLayout();
                previewContainer.repaint();
                previewContainer.getParent().getParent().validate();
            }
        };

        runner.start();
    }

    private void doPrint() {

        printingSupport.print(printable, printJobName);
    }

    private void showPageSetup() {

        PageFormat newFormat = printingSupport.pageSetup();

        if (newFormat == null)
            return;

        format = newFormat;

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                if (printable instanceof TablePrinter) {
                    ((TablePrinter) printable).reset();
                }

                previewContainer.invalidate();
                previewContainer.removeAll();
                generatePreviewImage();
                previewContainer.revalidate();
                repaint();
                GUIUtilities.scheduleGC();
            }

        });

    }

    private void generatePreviewImage() {

//        int orientation = format.getOrientation();

        m_wPage = (int) (format.getWidth());
        m_hPage = (int) (format.getHeight());

        int scale = 50;
        int w = (int) (m_wPage * scale / 100);
        int h = (int) (m_hPage * scale / 100);

        int pageIndex = 0;

        try {

            while (true) {
                BufferedImage img = new BufferedImage(m_wPage, m_hPage,
                        BufferedImage.TYPE_INT_RGB);
                Graphics g = img.getGraphics();
                g.setColor(Color.white);
                g.fillRect(0, 0, m_wPage, m_hPage);

                if (printable.print(g, format, pageIndex) != Printable.PAGE_EXISTS) {
                    break;
                }

                PagePreview pp = new PagePreview(w, h, img);
                previewContainer.add(pp);
                pageIndex++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Printing error: " + e.toString());
        }
    }


    class PreviewContainer extends JPanel {

        protected int H_GAP = 5;
        protected int V_GAP = 5;

        public Dimension getPreferredSize() {
            int n = getComponentCount();

            if (n == 0)
                return new Dimension(H_GAP, V_GAP);

            Component comp = getComponent(0);
            Dimension dc = comp.getPreferredSize();
            int w = dc.width;
            int h = dc.height;

            Dimension dp = getParent().getSize();
            int nCol = Math.max((dp.width - H_GAP) / (w + H_GAP), 1);
            int nRow = n / nCol;

            if (nRow * nCol < n)
                nRow++;

            int ww = nCol * (w + H_GAP) + H_GAP;
            int hh = nRow * (h + V_GAP) + V_GAP;
            Insets ins = getInsets();

            return new Dimension(ww + ins.left + ins.right, hh + ins.top + ins.bottom);
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public void doLayout() {
            Insets ins = getInsets();
            int x = ins.left + H_GAP;
            int y = ins.top + V_GAP;

            int n = getComponentCount();

            if (n == 0)
                return;

            Component comp = getComponent(0);
            Dimension dc = comp.getPreferredSize();
            int w = dc.width;
            int h = dc.height;

            Dimension dp = getParent().getSize();
            int nCol = Math.max((dp.width - H_GAP) / (w + H_GAP), 1);
            int nRow = n / nCol;

            if (nRow * nCol < n)
                nRow++;

            int index = 0;

            for (int k = 0; k < nRow; k++) {

                for (int m = 0; m < nCol; m++) {

                    if (index >= n)
                        return;

                    comp = getComponent(index++);
                    comp.setBounds(x, y, w, h);
                    x += w + H_GAP;

                }

                y += h + V_GAP;
                x = ins.left + H_GAP;

            }

        }

    } // class PreviewContainer


    class PagePreview extends JPanel {

        protected int m_w;
        protected int m_h;
        protected Image m_source;
        protected Image m_img;

        public PagePreview(int w, int h, Image source) {
            m_w = w;
            m_h = h;
            m_source = source;
            m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
            m_img.flush();
            setBackground(Color.white);
            setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
        }

        public void setScaledSize(int w, int h) {
            m_w = w;
            m_h = h;
            m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
            repaint();
        }

        public Dimension getPreferredSize() {
            Insets ins = getInsets();
            return new Dimension(m_w + ins.left + ins.right,
                    m_h + ins.top + ins.bottom);
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public void paint(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(m_img, 0, 0, this);
            paintBorder(g);
        }


    } // class PagePreview
    public static String bundledString(String key) {
        return Bundles.get(PrintPreviewer.class, key);
    }
}











