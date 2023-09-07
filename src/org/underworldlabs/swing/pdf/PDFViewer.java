package org.underworldlabs.swing.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class PDFViewer extends JPanel {

    private REPdfRenderer renderer;
    private JPanel panelSelectedPage;

    private int numberOfPages;
    private int currentPageIndex = 0;
    private JTextField txtPageNumber;
    private JButton btnLastPage;
    private JButton btnNextPage;
    private JButton btnPreviousPage;
    private JButton btnFirstPage;

    public PDFViewer(File document) throws Exception {
        PDDocument doc = Loader.loadPDF(document);
        initialize(doc);
    }

    public PDFViewer(byte[] data) throws Exception {
        PDDocument doc = Loader.loadPDF(data);
        initialize(doc);
    }

    private void enableDisableButtons(int actionIndex) {
        switch (actionIndex) {
            case 0:
                btnFirstPage.setEnabled(false);
                btnPreviousPage.setEnabled(false);
                btnNextPage.setEnabled(true);
                btnLastPage.setEnabled(true);
                break;
            case 1:
                btnFirstPage.setEnabled(true);
                btnPreviousPage.setEnabled(true);
                btnNextPage.setEnabled(false);
                btnLastPage.setEnabled(false);
                break;
            default:
                btnFirstPage.setEnabled(true);
                btnPreviousPage.setEnabled(true);
                btnNextPage.setEnabled(true);
                btnLastPage.setEnabled(true);
        }
    }

    private void selectPage(int pageIndex) {
        ImageIcon renderImage = null;

        try {
            renderImage = new ImageIcon(renderer.renderImage(pageIndex));
        } catch (IOException e) {
            e.printStackTrace();
        }
        panelSelectedPage.removeAll(); // Remove children

        JLabel imagePanel = new JLabel(renderImage);
        imagePanel.setBorder(BorderFactory.createTitledBorder("PDF"));

        //imagePanel.setPreferredSize(new Dimension(200, 200));
        panelSelectedPage.add(imagePanel, BorderLayout.CENTER);
        currentPageIndex = pageIndex;

        String pageText = String.format("Page: %d / %d", pageIndex + 1, numberOfPages);
        txtPageNumber.setText(pageText);

        if (pageIndex == 0) {
            enableDisableButtons(0);
        } else if (pageIndex == (numberOfPages - 1)) {
            enableDisableButtons(1);
        } else {
            enableDisableButtons(-1);
        }

        panelSelectedPage.revalidate();
        panelSelectedPage.repaint();

        //System.out.println(imagePanel.getPreferredSize().width + " : " + imagePanel.getPreferredSize().height);
    }

    private void initialize(PDDocument doc) {

        // Getting/calculating screen dimensions...


        numberOfPages = doc.getNumberOfPages();

        renderer = new REPdfRenderer(doc);


        setLayout(new BorderLayout());
        JPanel panelControls = new JPanel();
        add(panelControls, BorderLayout.SOUTH);
        panelControls.setLayout(new BorderLayout(0, 0));

        Component verticalStrutTop = Box.createVerticalStrut(10);
        panelControls.add(verticalStrutTop, BorderLayout.NORTH);

        Box horizontalBoxControls = Box.createHorizontalBox();
        panelControls.add(horizontalBoxControls);

        Component horizontalStrutLeft = Box.createHorizontalStrut(10);
        horizontalBoxControls.add(horizontalStrutLeft);

        btnFirstPage = new JButton("First Page");
        btnFirstPage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                selectPage(0);
            }
        });
        horizontalBoxControls.add(btnFirstPage);

        Component horizontalStrutLeft_1 = Box.createHorizontalStrut(10);
        horizontalBoxControls.add(horizontalStrutLeft_1);

        btnPreviousPage = new JButton("Previous Page");
        btnPreviousPage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentPageIndex > 0) {
                    selectPage(currentPageIndex - 1);
                }
            }
        });
        horizontalBoxControls.add(btnPreviousPage);

        Component horizontalStrutLeft_2 = Box.createHorizontalStrut(10);
        horizontalBoxControls.add(horizontalStrutLeft_2);

        txtPageNumber = new JTextField();
        horizontalBoxControls.add(txtPageNumber);
        txtPageNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPageNumber.setEditable(false);
        txtPageNumber.setPreferredSize(new Dimension(50, txtPageNumber.getPreferredSize().width));
        txtPageNumber.setColumns(10);

        Component horizontalStrutRight_2 = Box.createHorizontalStrut(10);
        horizontalBoxControls.add(horizontalStrutRight_2);

        btnNextPage = new JButton("Next Page");
        btnNextPage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentPageIndex < (numberOfPages - 1)) {
                    selectPage(currentPageIndex + 1);
                }
            }
        });
        horizontalBoxControls.add(btnNextPage);

        Component horizontalStrutRight_1 = Box.createHorizontalStrut(10);
        horizontalBoxControls.add(horizontalStrutRight_1);

        btnLastPage = new JButton("Last Page");
        btnLastPage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectPage(numberOfPages - 1);
            }
        });
        horizontalBoxControls.add(btnLastPage);

        Component horizontalStrutRight = Box.createHorizontalStrut(10);
        horizontalBoxControls.add(horizontalStrutRight);

        Component verticalStrutBottom = Box.createVerticalStrut(10);
        panelControls.add(verticalStrutBottom, BorderLayout.SOUTH);

        Box verticalBoxView = Box.createVerticalBox();
        add(verticalBoxView, BorderLayout.WEST);

        Component verticalStrutView = Box.createVerticalStrut(10);
        verticalBoxView.add(verticalStrutView);

        Box horizontalBoxView = Box.createHorizontalBox();
        verticalBoxView.add(horizontalBoxView);

        Component horizontalStrutViewLeft = Box.createHorizontalStrut(10);
        horizontalBoxView.add(horizontalStrutViewLeft);

        panelSelectedPage = new JPanel();
        panelSelectedPage.setBackground(Color.LIGHT_GRAY);
        horizontalBoxView.add(new JScrollPane(panelSelectedPage));
        panelSelectedPage.setBorder(new EmptyBorder(0, 0, 0, 0));
        panelSelectedPage.setLayout(new BorderLayout(0, 0));

        Component horizontalStrutViewRight = Box.createHorizontalStrut(10);
        horizontalBoxView.add(horizontalStrutViewRight);

        selectPage(0);
    }
}
