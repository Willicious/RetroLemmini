/*
 * Copyright 2026 Will James
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lemmini.gui;

import java.awt.Frame;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import lemmini.LemminiFrame;
import lemmini.game.Core;
import lemmini.game.ReplayChecker;

/**
 * Dialog for displaying the result of the replay check.
 * @author Will James
 */
public class ReplayCheckResultDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private List<ReplayChecker.ReplayCheckResult> replayResults;
    private ReplayChecker.ReplayCheckResult selectedResult;
    
    public enum DialogResult {
        LOAD_REPLAY,
        CANCEL
    }

    private DialogResult dialogResult = DialogResult.CANCEL;

    public DialogResult getResult() {
        return dialogResult;
    }
    
    public ReplayChecker.ReplayCheckResult getSelectedResult() {
        return selectedResult;
    }

    /**
     * Creates new form ReplayDialog
     */
    public ReplayCheckResultDialog(Frame parent, boolean modal, List<ReplayChecker.ReplayCheckResult> replayResults) {
        super(parent, modal);
        this.replayResults = replayResults;
        this.selectedResult = null;

        initComponents();
        populateTable();

        setMinimumSize(getSize());
        setSize(parent.getWidth() - 40, parent.getHeight() - 40);
        setLocationRelativeTo(parent);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        jScrollPaneReplays = new javax.swing.JScrollPane();
        replayTableModel = new javax.swing.table.DefaultTableModel(new Object[] {"Replay", "Result", "Saved", "Time"}, 0) {
			private static final long serialVersionUID = 1L;
				@Override
        	    public boolean isCellEditable(int row, int column) {
        	        return false;
        	    }
        	};
        jTableReplays = new javax.swing.JTable(replayTableModel);
        jButtonLoadReplay = new javax.swing.JButton();
        jButtonDeleteReplays = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Batch Replay Check Results");
        setIconImage(Toolkit.getDefaultToolkit().getImage(LemminiFrame.class.getClassLoader().getResource("icon_256.png")));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jScrollPaneReplays.setViewportView(jTableReplays);

        jButtonLoadReplay.setText("Load Replay");
        jButtonLoadReplay.setEnabled(false);
        jButtonLoadReplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadReplayActionPerformed(evt);
            }
        });
        
        jButtonDeleteReplays.setText("Delete Replay");
        jButtonDeleteReplays.setEnabled(false);
        jButtonDeleteReplays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteReplaysActionPerformed(evt);
            }
        });
        
        jTableReplays.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int count = jTableReplays.getSelectedRowCount();
            jButtonLoadReplay.setEnabled(count == 1);
            jButtonDeleteReplays.setEnabled(count > 0);
            jButtonDeleteReplays.setText(count == 1 ? "Delete Replay" : "Delete Replays");
        });  
        javax.swing.table.TableColumnModel columnModel = jTableReplays.getColumnModel();     
        jTableReplays.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        columnModel.getColumn(0).setPreferredWidth(300);
        columnModel.getColumn(1).setPreferredWidth(80);
        columnModel.getColumn(2).setPreferredWidth(80);
        columnModel.getColumn(3).setPreferredWidth(80);

        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneReplays)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonLoadReplay)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonDeleteReplays)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 112, Short.MAX_VALUE)
                        .addComponent(jButtonClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneReplays, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonLoadReplay)
                    .addComponent(jButtonDeleteReplays)
                    .addComponent(jButtonClose))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void populateTable() {
        replayTableModel.setRowCount(0);
        for (ReplayChecker.ReplayCheckResult result : replayResults) {
            replayTableModel.addRow(new Object[] {
                result.getReplayPath().getFileName().toString(),
                result.getReplayResult(),
                result.getLemsSaved() + " / " + result.getSaveRequirement(),
                getTimeString(result)
            });
        }
    }
    
    private String getTimeString(ReplayChecker.ReplayCheckResult result) {
    	if (result.getReplayResult() == ReplayChecker.ReplayResult.PASS) {
    	    int t = result.getTimeElapsed();
    	    return String.format("%d:%02d", t / 60, t % 60);
    	} else {
    	    return "—";
    	}
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    	replayResults.clear();
    	replayResults = null;
    }//GEN-LAST:event_formWindowClosing
    
    private void jButtonLoadReplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadReplayActionPerformed
        int row = jTableReplays.getSelectedRow();
        if (row < 0 || row >= replayResults.size()) return;
        selectedResult = replayResults.get(row);
    	replayResults.clear();
    	replayResults = null;
        dispose();
    }//GEN-LAST:event_jButtonLoadReplayActionPerformed
    
    private void jButtonDeleteReplaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteReplaysActionPerformed
        int[] rows = jTableReplays.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        
        // Confirm
        String message = (rows.length == 1)
                ? "Are you sure you want to move the selected replay to\n\n resources/replays/deleted_replays?"
                : "Are you sure you want to move the " + rows.length + " selected replays to\n\n resources/replays/deleted_replays?";
        int response = JOptionPane.showConfirmDialog(this, message, "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (response != JOptionPane.YES_OPTION) return;

        // Delete
        try {
        	Path deletedFolder = Core.resourcePath.resolve(Core.REPLAYS_PATH).resolve("deleted_replays");
            Files.createDirectories(deletedFolder);
            for (int i = rows.length - 1; i >= 0; i--) {
                int row = rows[i];
                ReplayChecker.ReplayCheckResult result = replayResults.get(row);
                Path source = result.getReplayPath();
                Path target = deletedFolder.resolve(source.getFileName());
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                replayResults.remove(row);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "One or more replay files could not be moved.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        // Repopulate table
        populateTable();
    }//GEN-LAST:event_jButtonDeleteReplaysActionPerformed

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
    	replayResults.clear();
    	replayResults = null;
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * Get selected list index.
     */
    public int getSelection() {
        return jListReplays.getSelectedIndex();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPaneReplays;
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonLoadReplay;
    private javax.swing.JButton jButtonDeleteReplays;
    private javax.swing.JList<String> jListReplays;
    private javax.swing.JTable jTableReplays;
    private javax.swing.table.DefaultTableModel replayTableModel;
    // End of variables declaration//GEN-END:variables
}
