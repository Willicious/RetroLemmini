/*
 * FILE MODIFIED BY RYAN SAKOWSKI
 * 
 * 
 * Copyright 2009 Volker Oth
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
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import lemmini.LemminiFrame;
import lemmini.game.Core;

/**
 * Dialog for managing players.
 * @author Volker Oth
 */
public class PlayerDialog extends JDialog {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector<String> players;
    
    /**
     * Creates new form PlayerDialog
     * @param parent
     * @param modal
     */
    public PlayerDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setMinimumSize(getSize());
        setLocationRelativeTo(parent);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPanePlayers = new javax.swing.JScrollPane();
        players = new Vector<>();
        for (int i = 0; i < Core.getPlayerCount(); i++) {
            players.add(Core.getPlayer(i));
        }
        jListPlayers = new JList<String>(players);
        jListPlayers.setSelectedValue(Core.player.getName(), true);
        jButtonNew = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Manage Players");
        setIconImage(Toolkit.getDefaultToolkit().getImage(LemminiFrame.class.getClassLoader().getResource("icon_256.png")));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jScrollPanePlayers.setViewportView(jListPlayers);

        jButtonNew.setText("New Player...");
        jButtonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewActionPerformed(evt);
            }
        });

        jButtonDelete.setText("Delete Selected Players");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPanePlayers)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 112, Short.MAX_VALUE)
                        .addComponent(jButtonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPanePlayers, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonNew)
                    .addComponent(jButtonDelete)
                    .addComponent(jButtonOK)
                    .addComponent(jButtonCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        players.clear();
        players = null;
    }//GEN-LAST:event_formWindowClosing
    
    private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewActionPerformed
        String player = JOptionPane.showInputDialog(
                LemminiFrame.getFrame(), "Enter Player Name", "Input", JOptionPane.QUESTION_MESSAGE);
        if (player != null) {
            // check if this player already exists
            // if it already exists, reset the existing profile
            boolean found = false;
            for (String p : players) {
                if (p.toLowerCase(Locale.ROOT).equals(player.toLowerCase(Locale.ROOT))) {
                    player = p;
                    found = true;
                    break;
                }
            }
            // really a new player
            if (!found) {
                players.add(player);
                jListPlayers.setListData(players);
                int i = players.size() - 1;
                if (i >= 0) {
                    jListPlayers.setSelectedIndex(i);
                }
            }
        }
    }//GEN-LAST:event_jButtonNewActionPerformed
    
    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        int[] indices = jListPlayers.getSelectedIndices();
        for (int i = indices.length - 1; i >= 0; i--) {
            int idx = indices[i];
            Core.deletePlayer(idx);
            players.remove(idx);
        }
        jListPlayers.setListData(players);
    }//GEN-LAST:event_jButtonDeleteActionPerformed
    
    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        dispose();
    }//GEN-LAST:event_jButtonOKActionPerformed
    
    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        players.clear();
        players = null;
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed
    
    /**
     * Get list of players.
     * @return list of players.
     */
    public List<String> getPlayers() {
        return players;
    }
    
    /**
     * Get selected list index.
     * @return selected list index
     */
    public int getSelection() {
        return jListPlayers.getSelectedIndex();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonNew;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JList<String> jListPlayers;
    private javax.swing.JScrollPane jScrollPanePlayers;
    // End of variables declaration//GEN-END:variables
}
