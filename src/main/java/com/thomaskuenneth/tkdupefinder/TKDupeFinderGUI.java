/*
 * @(#)TKDupeFinderGUI.java	0.02, 2016/06/26
 * Copyright 2008 - 2016 Thomas Kuenneth
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thomaskuenneth.tkdupefinder;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * Diese Klasse realisiert eine (einfache) Benutzeroberfläche für die Klasse
 * <code>TKDupeFinder</code>.
 * <p>
 *
 * @author Thomas Künneth
 * @version 0.02, 2016/06/26
 */
public class TKDupeFinderGUI extends JFrame implements ActionListener,
        ListSelectionListener {

    /**
     * generiert aus Version 0.01a, 2008/01/25
     */
    private static final long serialVersionUID = -2443092285414016843L;

    private TKDupeFinder df;
    private String[] checksums = {};
    private int currentPos;

    private JTextField textfieldBasedir;
    private JPanel panelBasedir, panelHeader, panelContents, panelFiles,
            panelButtons;
    private JButton buttonBasedir, buttonPrev, buttonNext, buttonShow,
            buttonDelete;
    private JLabel labelInfo;
    private JList<File> listFiles;
    private JScrollPane scrollpaneFiles;
    private DefaultListModel<File> modelFiles;

    public TKDupeFinderGUI() {
        super("Dubletten finden");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        createComponents();
        setContentPane(buildContentPane());
        pack();
        setLocationRelativeTo(null);
        try {
            df = new TKDupeFinder();
            updateGUI();
            labelInfo
                    .setText("<html>Bitte die Schaltfläche <strong>Suchen</strong> anklicken");
        } catch (NoSuchAlgorithmException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
    }

    private void createComponents() {
        // component to select the base directory
        panelBasedir = new JPanel(new BorderLayout(10, 0));
        textfieldBasedir = new JTextField(System.getProperty("user.dir"));
        textfieldBasedir.addActionListener(this);
        panelBasedir.add(textfieldBasedir, BorderLayout.CENTER);
        buttonBasedir = new JButton("Suchen");
        buttonBasedir.addActionListener(this);
        panelBasedir.add(buttonBasedir, BorderLayout.EAST);
        // a header panel and its components
        panelHeader = new JPanel(new FlowLayout(FlowLayout.LEADING));
        buttonPrev = new BasicArrowButton(BasicArrowButton.WEST);
        buttonPrev.addActionListener(this);
        panelHeader.add(buttonPrev);
        buttonNext = new BasicArrowButton(BasicArrowButton.EAST);
        buttonNext.addActionListener(this);
        panelHeader.add(buttonNext);
        labelInfo = new JLabel();
        panelHeader.add(labelInfo);
        // the contents panel
        panelContents = new JPanel(new BorderLayout());
        panelContents.add(panelHeader, BorderLayout.NORTH);
        modelFiles = new DefaultListModel<>();
        listFiles = new JList<File>(modelFiles);
        listFiles.addListSelectionListener(this);
        scrollpaneFiles = new JScrollPane(listFiles);
        panelFiles = new JPanel(new BorderLayout(10, 0));
        panelFiles.add(scrollpaneFiles, BorderLayout.CENTER);
        JPanel p = new JPanel(new GridLayout(2, 1));
        buttonShow = new JButton("Anzeigen");
        buttonShow.addActionListener(this);
        p.add(buttonShow);
        buttonDelete = new JButton("Löschen");
        buttonDelete.addActionListener(this);
        p.add(buttonDelete);
        panelButtons = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        panelButtons.add(p);
        panelFiles.add(panelButtons, BorderLayout.EAST);
        panelContents.add(panelFiles, BorderLayout.CENTER);
    }

    private JPanel buildContentPane() {
        JPanel cp = new JPanel(new BorderLayout());
        cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cp.add(panelBasedir, BorderLayout.NORTH);
        cp.add(panelContents, BorderLayout.CENTER);
        return cp;
    }

    public void setupContents() {
        df.clear();
        df.scanDir(textfieldBasedir.getText(), true);
        df.removeSingles();
        checksums = df.getChecksums();
        updateGUI();
    }

    private void updateGUI() {
        boolean enabled = checksums.length > 1;
        buttonPrev.setEnabled(enabled);
        buttonNext.setEnabled(enabled);
        currentPos = 0;
        updateContents(0);
    }

    private void updateContents(int offset) {
        modelFiles.removeAllElements();
        if (checksums.length < 1) {
            labelInfo.setText("keine Dubletten gefunden");
        } else {
            currentPos += offset;
            if (currentPos >= checksums.length) {
                currentPos = 0;
            } else if (currentPos < 0) {
                currentPos = checksums.length - 1;
            }
            List<File> files = df.getFiles(checksums[currentPos]);
            files.stream().forEach((f) -> {
                modelFiles.addElement(f);
            });
            labelInfo.setText(Integer.toString(currentPos + 1) + " von "
                    + Integer.toString(checksums.length));
        }
        listFiles.getSelectionModel().setSelectionInterval(1, modelFiles.getSize() - 1);
        updateButtons();
    }

    private void updateButtons() {
        int[] selection = listFiles.getSelectedIndices();
        boolean enabled = (selection.length > 0);
        buttonShow.setEnabled(enabled);
        buttonDelete.setEnabled(enabled);
    }

    private void showSelectedFiles() {
        int[] selection = listFiles.getSelectedIndices();
        for (int index : selection) {
            Object o = modelFiles.get(index);
            if (o instanceof File) {
                try {
                    Desktop.getDesktop().open((File) o);
                } catch (IOException ex) {
                    System.err.println(ex.getLocalizedMessage());
                }
            }
        }
    }

	private void deleteSelectedFiles() {
		int[] selection = listFiles.getSelectedIndices();
		for (int index : selection) {
			File file = modelFiles.get(index);
			if (df.deleteFile(checksums[currentPos], file)) {
				modelFiles.removeElement(file);
			}
		}
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (buttonPrev.equals(src)) {
            updateContents(-1);
        } else if (buttonNext.equals(src)) {
            updateContents(1);
        } else if (buttonShow.equals(src)) {
            showSelectedFiles();
        } else if (buttonDelete.equals(src)) {
            deleteSelectedFiles();
        } else if ((textfieldBasedir.equals(src))
                || (buttonBasedir.equals(src))) {
            setupContents();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        updateButtons();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException |
                    IllegalAccessException | UnsupportedLookAndFeelException thr) {
                System.err.println(thr.getLocalizedMessage());
            }
            TKDupeFinderGUI me = new TKDupeFinderGUI();
            me.setVisible(true);
        });
    }
}
