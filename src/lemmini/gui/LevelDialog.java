/*
 * Copyright 2014 Ryan Sakowski.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lemmini.gui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.GroupLayout;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import lemmini.LemminiFrame;
import lemmini.game.Core;
import lemmini.game.GameController;
import lemmini.game.LevelInfo;
import lemmini.game.LevelPack;
import lemmini.game.LevelRecord;
import lemmini.game.MiscGfx;
import lemmini.graphics.LemmImage;
import lemmini.tools.ToolBox;

/**
 *
 * @author Ryan Sakowski, William James
 */
public class LevelDialog extends JDialog {
	
	/** Custom class for storing/displaying level nodes */
	class LevelItem {
	    final int levelPack;
	    final int rating;
	    final int levelIndex;
	    final String levelName;
	    final boolean completed;

	    LevelItem(int lp, int r, int li, String ln, boolean c) {
	        levelPack = lp;
	        rating = r;
	        levelIndex = li;
	        levelName = ln;
	        completed = c;
	    }

	    @Override
	    public String toString() {
	        return (levelIndex + 1) + ": " + levelName + (completed ? " (completed)" : StringUtils.EMPTY);
	    }
	}
	
	/** Custom tree renderer */
	class LevelTreeRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;

	    @Override
	    public Component getTreeCellRendererComponent(
	            JTree tree, Object value, boolean sel, boolean expanded,
	            boolean leaf, int row, boolean hasFocus) {

	        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

	        if (!(value instanceof DefaultMutableTreeNode)) return this;

	        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
	        Object userObj = node.getUserObject();

	        if (userObj instanceof LevelPack) {
	            LevelPack lp = (LevelPack) userObj;

	            boolean fullyCompleted = true;
	            for (int r = 0; r < lp.getRatings().size(); r++) {
	                if (isAnyLevelIncomplete(lp, r)) {
	                    fullyCompleted = false;
	                    break;
	                }
	            }
	            setText(lp.getName() + (fullyCompleted ? " (completed)" : ""));
	        }
	        else if (userObj instanceof String) {
	            String ratingName = (String) userObj;

	            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
	            LevelPack lp = getPackFromTreeNode(parentNode);

	            if (lp != null) {
	                int ratingIndex = parentNode.getIndex(node);
	                boolean fullyCompleted = !isAnyLevelIncomplete(lp, ratingIndex) && isAnyLevelComplete(lp, ratingIndex);
	                setText(ratingName + (fullyCompleted ? " (completed)" : ""));
	            } else {
	                setText(ratingName);
	            }
	        }
	        // LevelItem nodes: currently handled by toString() override in the LevelItem class

	        return this;
	    }
	}

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static Path lvlPath = Paths.get(".");
    
    private Path currentPackFolder = null;

    private DefaultMutableTreeNode topNode = null;
    private DefaultTreeModel levelModel = null;
    private LevelItem selectedLevel = null;

    private int[] levelPackPositionLookup;
    private int[][] ratingPositionLookup;
    private int[][][] levelPositionLookup;

    /**
     * Creates new form LevelDialog
     * @param parent
     * @param modal
     */
    public LevelDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setMinimumSize(getSize());
        setLocationRelativeTo(parent);
        currentPackFolder = null;
        fillInInfo();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPaneLevels = new javax.swing.JScrollPane();
        topNode = new DefaultMutableTreeNode("Levels");
        levelModel = new DefaultTreeModel(topNode);
        refreshLevels();
        jTreeLevels = new javax.swing.JTree();
        jTreeLevels.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTreeLevels.setCellRenderer(new LevelTreeRenderer());
        jLabelFloaterImage = new javax.swing.JLabel();
        jLabelLogoImage = new javax.swing.JLabel();
        jPanelContent = new javax.swing.JPanel();
        jLabelAuthor = new javax.swing.JLabel();
        jTextFieldAuthor = new javax.swing.JTextField();
        jPanelLevelInfo = new javax.swing.JPanel();
        jLabelNumLemmings = new javax.swing.JLabel();
        jTextFieldNumLemmings = new javax.swing.JTextField();
        jLabelNumToRescue = new javax.swing.JLabel();
        jTextFieldNumToRescue = new javax.swing.JTextField();
        jLabelReleaseRate = new javax.swing.JLabel();
        jTextFieldReleaseRate = new javax.swing.JTextField();
        jLabelTimeLimit = new javax.swing.JLabel();
        jTextFieldTimeLimit = new javax.swing.JTextField();
        jSeparatorSkills = new javax.swing.JSeparator();
        jSeparatorRecords = new javax.swing.JSeparator();
        jLabelNumClimbers = new javax.swing.JLabel();
        jTextFieldNumClimbers = new javax.swing.JTextField();
        jLabelNumFloaters = new javax.swing.JLabel();
        jTextFieldNumFloaters = new javax.swing.JTextField();
        jLabelNumBombers = new javax.swing.JLabel();
        jTextFieldNumBombers = new javax.swing.JTextField();
        jLabelNumBlockers = new javax.swing.JLabel();
        jTextFieldNumBlockers = new javax.swing.JTextField();
        jLabelNumBuilders = new javax.swing.JLabel();
        jTextFieldNumBuilders = new javax.swing.JTextField();
        jLabelNumBashers = new javax.swing.JLabel();
        jTextFieldNumBashers = new javax.swing.JTextField();
        jLabelNumMiners = new javax.swing.JLabel();
        jTextFieldNumMiners = new javax.swing.JTextField();
        jLabelNumDiggers = new javax.swing.JLabel();
        jTextFieldNumDiggers = new javax.swing.JTextField();
        jPanelRecords = new javax.swing.JPanel();
        jLabelLemmingsSaved = new javax.swing.JLabel();
        jTextFieldLemmingsSaved = new javax.swing.JTextField();
        jLabelSkillsUsed = new javax.swing.JLabel();
        jTextFieldSkillsUsed = new javax.swing.JTextField();
        jLabelTimeElapsed = new javax.swing.JLabel();
        jTextFieldTimeElapsed = new javax.swing.JTextField();
        jLabelScore = new javax.swing.JLabel();
        jTextFieldScore = new javax.swing.JTextField();
        jButtonAddExternalLevels = new javax.swing.JButton();
        jButtonClearExternalLevels = new javax.swing.JButton();
        jButtonGetMoreLevels = new javax.swing.JButton();
        jButtonGetMoreMusic = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Choose Level");
        setIconImage(Toolkit.getDefaultToolkit().getImage(LemminiFrame.class.getClassLoader().getResource("icon_256.png")));

        jTreeLevels.setModel(levelModel);
        jTreeLevels.setRootVisible(false);
        jTreeLevels.setShowsRootHandles(true);
        jTreeLevels.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTreeLevelsMousePressed(evt);
            }
        });
        jTreeLevels.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeLevelsValueChanged(evt);
            }
        });
        jTreeLevels.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTreeLevelsKeyPressed(evt);
            }
        });
        jScrollPaneLevels.setViewportView(jTreeLevels);
        selectCurrentLevel();
        
        LemmImage logo = MiscGfx.getImage(MiscGfx.Index.RETROLEMMINI_LOGO_AMIGA);
        jLabelLogoImage.setIcon(new ImageIcon(getScaledImage(logo.getImage(), 300, 80)));
        jLabelLogoImage.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelLogoImage.setVerticalAlignment(SwingConstants.CENTER);

        jLabelAuthor.setText("Author:");

        jTextFieldAuthor.setEditable(false);
        jTextFieldAuthor.setHighlighter(null);

        jPanelLevelInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Level info", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jLabelNumLemmings.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelNumLemmings.setText("Number of Lemmings:");

        jTextFieldNumLemmings.setEditable(false);
        jTextFieldNumLemmings.setHighlighter(null);

        jLabelNumToRescue.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelNumToRescue.setText("Lemmings to be saved:");

        jTextFieldNumToRescue.setEditable(false);
        jTextFieldNumToRescue.setHighlighter(null);

        jLabelReleaseRate.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelReleaseRate.setText("Release rate:");

        jTextFieldReleaseRate.setEditable(false);
        jTextFieldReleaseRate.setHighlighter(null);

        jLabelTimeLimit.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelTimeLimit.setText("Time limit:");

        jTextFieldTimeLimit.setEditable(false);
        jTextFieldTimeLimit.setHighlighter(null);

        jLabelNumClimbers.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelNumClimbers.setText("Climbers:");

        jTextFieldNumClimbers.setEditable(false);
        jTextFieldNumClimbers.setHighlighter(null);

        jLabelNumFloaters.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelNumFloaters.setText("Floaters:");

        jTextFieldNumFloaters.setEditable(false);
        jTextFieldNumFloaters.setHighlighter(null);

        jLabelNumBombers.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelNumBombers.setText("Bombers:");

        jTextFieldNumBombers.setEditable(false);
        jTextFieldNumBombers.setHighlighter(null);

        jLabelNumBlockers.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelNumBlockers.setText("Blockers:");

        jTextFieldNumBlockers.setEditable(false);
        jTextFieldNumBlockers.setHighlighter(null);

        jLabelNumBuilders.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelNumBuilders.setText("Builders:");

        jTextFieldNumBuilders.setEditable(false);
        jTextFieldNumBuilders.setHighlighter(null);

        jLabelNumBashers.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelNumBashers.setText("Bashers:");

        jTextFieldNumBashers.setEditable(false);
        jTextFieldNumBashers.setHighlighter(null);

        jLabelNumMiners.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelNumMiners.setText("Miners:");

        jTextFieldNumMiners.setEditable(false);
        jTextFieldNumMiners.setHighlighter(null);

        jLabelNumDiggers.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelNumDiggers.setText("Diggers:");

        jTextFieldNumDiggers.setEditable(false);
        jTextFieldNumDiggers.setHighlighter(null);

        javax.swing.GroupLayout jPanelLevelInfoLayout = new javax.swing.GroupLayout(jPanelLevelInfo);
        jPanelLevelInfo.setLayout(jPanelLevelInfoLayout);
        jPanelLevelInfoLayout.setHorizontalGroup(
            jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLevelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparatorSkills)
                    .addGroup(jPanelLevelInfoLayout.createSequentialGroup()
                        .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelReleaseRate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelNumLemmings, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelTimeLimit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelNumToRescue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldTimeLimit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldReleaseRate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldNumToRescue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldNumLemmings, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLevelInfoLayout.createSequentialGroup()
                        .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelNumDiggers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelNumMiners, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelNumBashers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelNumBuilders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelNumBlockers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelNumBombers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelNumFloaters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelNumClimbers, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldNumClimbers, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldNumFloaters, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldNumBombers, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldNumBlockers, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldNumBuilders, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldNumBashers, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldNumMiners, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldNumDiggers, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap()));
        jPanelLevelInfoLayout.setVerticalGroup(
            jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLevelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelNumLemmings)
                    .addComponent(jTextFieldNumLemmings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelNumToRescue)
                    .addComponent(jTextFieldNumToRescue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelReleaseRate)
                    .addComponent(jTextFieldReleaseRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTimeLimit)
                    .addComponent(jTextFieldTimeLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorSkills, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelNumClimbers)
                    .addComponent(jTextFieldNumClimbers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelNumFloaters)
                    .addComponent(jTextFieldNumFloaters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelNumBombers)
                    .addComponent(jTextFieldNumBombers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelNumBlockers)
                    .addComponent(jTextFieldNumBlockers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelNumBuilders)
                    .addComponent(jTextFieldNumBuilders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelNumBashers)
                    .addComponent(jTextFieldNumBashers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelNumMiners)
                    .addComponent(jTextFieldNumMiners, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLevelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelNumDiggers)
                    .addComponent(jTextFieldNumDiggers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelRecords.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Records", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jLabelLemmingsSaved.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelLemmingsSaved.setText("Most Lemmings saved:");

        jTextFieldLemmingsSaved.setEditable(false);
        jTextFieldLemmingsSaved.setHighlighter(null);

        jLabelSkillsUsed.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelSkillsUsed.setText("Fewest skills used:");

        jTextFieldSkillsUsed.setEditable(false);
        jTextFieldSkillsUsed.setHighlighter(null);

        jLabelTimeElapsed.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelTimeElapsed.setText("Best time (elapsed):");

        jTextFieldTimeElapsed.setEditable(false);
        jTextFieldTimeElapsed.setHighlighter(null);

        jLabelScore.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelScore.setText("Highest score:");

        jTextFieldScore.setEditable(false);
        jTextFieldScore.setHighlighter(null);
        
        LemmImage floaterLemming = MiscGfx.getImage(MiscGfx.Index.FLOATER_LEMMING);
        jLabelFloaterImage.setIcon(new ImageIcon(getScaledImage(floaterLemming.getImage(), 200, 200)));
        jLabelFloaterImage.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelFloaterImage.setVerticalAlignment(SwingConstants.CENTER);

        javax.swing.GroupLayout jPanelRecordsLayout = new javax.swing.GroupLayout(jPanelRecords);
        jPanelRecords.setLayout(jPanelRecordsLayout);
        jPanelRecordsLayout.setHorizontalGroup(
        	    jPanelRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        	    .addGroup(jPanelRecordsLayout.createSequentialGroup()
        	    	    .addContainerGap()
        	    	    .addComponent(jSeparatorRecords, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        	    	    .addContainerGap())
        	        .addGroup(jPanelRecordsLayout.createSequentialGroup()
        	            .addContainerGap()
        	            .addGroup(jPanelRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        	                .addGroup(jPanelRecordsLayout.createSequentialGroup()
        	                    .addGroup(jPanelRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        	                        .addComponent(jLabelScore, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        	                        .addComponent(jLabelTimeElapsed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        	                        .addComponent(jLabelSkillsUsed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        	                        .addComponent(jLabelLemmingsSaved, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        	                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        	                    .addGroup(jPanelRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
        	                        .addComponent(jTextFieldScore, 60, 60, 60)
        	                        .addComponent(jTextFieldTimeElapsed)
        	                        .addComponent(jTextFieldSkillsUsed)
        	                        .addComponent(jTextFieldLemmingsSaved)))
        	                .addComponent(jLabelFloaterImage, javax.swing.GroupLayout.DEFAULT_SIZE,
        	                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        	            .addContainerGap()));
        jPanelRecordsLayout.setVerticalGroup(
        	    jPanelRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        	        .addGroup(jPanelRecordsLayout.createSequentialGroup()
        	            .addContainerGap()
        	            .addGroup(jPanelRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        	                .addComponent(jLabelLemmingsSaved)
        	                .addComponent(jTextFieldLemmingsSaved, javax.swing.GroupLayout.PREFERRED_SIZE,
        	                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        	            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        	            .addGroup(jPanelRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        	                .addComponent(jLabelSkillsUsed)
        	                .addComponent(jTextFieldSkillsUsed, javax.swing.GroupLayout.PREFERRED_SIZE,
        	                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        	            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        	            .addGroup(jPanelRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        	                .addComponent(jLabelTimeElapsed)
        	                .addComponent(jTextFieldTimeElapsed, javax.swing.GroupLayout.PREFERRED_SIZE,
        	                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        	            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        	            .addGroup(jPanelRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        	                .addComponent(jLabelScore)
        	                .addComponent(jTextFieldScore, javax.swing.GroupLayout.PREFERRED_SIZE,
        	                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        	            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        	            .addComponent(jSeparatorRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        	            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        	            .addComponent(jLabelFloaterImage, javax.swing.GroupLayout.PREFERRED_SIZE,
        	                    javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        	            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        jButtonAddExternalLevels.setText("Add External Levels");
        jButtonAddExternalLevels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddExternalLevelsActionPerformed(evt);
            }
        });

        jButtonClearExternalLevels.setText("Clear External Levels");
        jButtonClearExternalLevels.setEnabled(GameController.externalLevelList.size() > 0);
        jButtonClearExternalLevels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearExternalLevelsActionPerformed(evt);
            }
        });
        
        jButtonGetMoreLevels.setText("Get More Levels");
        jButtonGetMoreLevels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetMoreLevelsActionPerformed(evt);
            }
        });
        
        jButtonGetMoreMusic.setText("Get More Music");
        jButtonGetMoreMusic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetMoreMusicActionPerformed(evt);
            }
        });

        jButtonOK.setText("Choose Level/Pack");
        jButtonOK.setEnabled(false);
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
        
        // Content layout
        jPanelContent.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY));
        GroupLayout contentLayout = new GroupLayout(jPanelContent);
        jPanelContent.setLayout(contentLayout);
        contentLayout.setHorizontalGroup(
        	    contentLayout.createSequentialGroup()
        	        .addContainerGap()
        	        .addGroup(contentLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	            .addComponent(jLabelLogoImage, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        	            .addGroup(contentLayout.createSequentialGroup()
        	                .addComponent(jLabelAuthor)
        	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        	                .addComponent(jTextFieldAuthor))
        	            .addGroup(contentLayout.createSequentialGroup()
        	                .addComponent(jPanelLevelInfo)
        	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        	                .addComponent(jPanelRecords)))
        	        .addContainerGap());
        contentLayout.setVerticalGroup(
        	    contentLayout.createSequentialGroup()
        	        .addContainerGap()	
        	        	.addComponent(jLabelLogoImage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(contentLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(jLabelAuthor)
	                    .addComponent(jTextFieldAuthor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addGroup(contentLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                    .addComponent(jPanelLevelInfo)
	                    .addComponent(jPanelRecords))
	                .addContainerGap());

        // Full dialog layout
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
        	    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	        .addGroup(layout.createSequentialGroup()
        	            .addContainerGap()
        	            .addComponent(jScrollPaneLevels)
        	            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        	            .addComponent(jPanelContent, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        	            .addContainerGap())
        	        .addGroup(layout.createSequentialGroup()
            	        .addContainerGap()	
        	            .addComponent(jButtonAddExternalLevels)
        	            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        	            .addComponent(jButtonClearExternalLevels)
        	            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        	            .addComponent(jButtonGetMoreLevels)
        	            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        	            .addComponent(jButtonGetMoreMusic)
        	            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 340, Short.MAX_VALUE)
        	            .addComponent(jButtonOK)
        	            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        	            .addComponent(jButtonCancel)
        	            .addContainerGap()));
        layout.setVerticalGroup(
        	    layout.createSequentialGroup()
        	        .addContainerGap()
        	        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	            .addComponent(jScrollPaneLevels)
        	            .addComponent(jPanelContent))
        	        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
        	        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	            .addComponent(jButtonAddExternalLevels)
        	            .addComponent(jButtonClearExternalLevels)
        	            .addComponent(jButtonGetMoreLevels)
        	            .addComponent(jButtonGetMoreMusic)
        	            .addComponent(jButtonCancel)
        	            .addComponent(jButtonOK))
        	        .addContainerGap());
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private LevelPack getPackFromTreeNode(DefaultMutableTreeNode packNode) {
        if (packNode == null) return null;
        Object userObj = packNode.getUserObject();
        if (userObj instanceof LevelPack) {
            return (LevelPack) userObj;
        }
        return null;
    }

    private void updatePackLogo(LevelPack pack) {
        if (pack == null) return;
        
        Path folder = GameController.getLevelPackFolder(GameController.levelPacks.indexOf(pack));
        if (folder == null) return;
        if (folder.equals(currentPackFolder) && jLabelLogoImage.getIcon() != null) return;
        
        currentPackFolder = folder;
        LemmImage logoImage;
        Path logoPath = folder.resolve("logo.png");
        if (Files.exists(logoPath)) {
            try {
                logoImage = new LemmImage(ImageIO.read(logoPath.toFile()));
            } catch (IOException e) {
                logoImage = MiscGfx.getImage(MiscGfx.Index.RETROLEMMINI_LOGO_AMIGA);
            }
        } else {
            logoImage = MiscGfx.getImage(MiscGfx.Index.RETROLEMMINI_LOGO_AMIGA);
        }
        Image scaled = getScaledImage(logoImage.getImage(), 300, 80);
        jLabelLogoImage.setIcon(new ImageIcon(scaled));
    }
    
    private void updateFloaterLem(boolean full, boolean partial) {
        LemmImage image =
            full ? MiscGfx.getImage(MiscGfx.Index.LEVEL_COMPLETED_GOLD) :
            partial ? MiscGfx.getImage(MiscGfx.Index.LEVEL_NOT_COMPLETED) :
            MiscGfx.getImage(MiscGfx.Index.FLOATER_LEMMING);

        jLabelFloaterImage.setIcon(new ImageIcon(getScaledImage(image.getImage(), 200, 200)));
    }

    private void jTreeLevelsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTreeLevelsKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            TreePath selPath = jTreeLevels.getSelectionPath();
            Object[] selPathArray = selPath.getPath();
            if (selPathArray.length >= 4) {
                selectedLevel = (LevelItem) ((DefaultMutableTreeNode) selPathArray[3]).getUserObject();
                dispose();
            } else {
                if (jTreeLevels.isExpanded(selPath)) {
                    jTreeLevels.collapsePath(selPath);
                } else {
                    jTreeLevels.expandPath(selPath);
                }
            }
        }
    }//GEN-LAST:event_jTreeLevelsKeyPressed

    private void jTreeLevelsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTreeLevelsMousePressed
        if (evt.getClickCount() == 2) {
            TreePath selPath = jTreeLevels.getPathForLocation(evt.getX(), evt.getY());
            if (selPath != null) {
                Object[] selPathArray = selPath.getPath();
                if (selPathArray.length >= 4) {
                    selectedLevel = (LevelItem) ((DefaultMutableTreeNode) selPathArray[3]).getUserObject();
                    dispose();
                }
            }
        }
    }//GEN-LAST:event_jTreeLevelsMousePressed

    private void jTreeLevelsValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreeLevelsValueChanged
        fillInInfo();
    }//GEN-LAST:event_jTreeLevelsValueChanged

    private void jButtonAddExternalLevelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddExternalLevelsActionPerformed
        List<Path> externLvls = ToolBox.getFileNames(this, lvlPath, true, true, Core.LEVEL_EXTENSIONS);
        if (externLvls != null) {
            if (!externLvls.isEmpty()) {
                lvlPath = externLvls.get(0).getParent();
            }
            int[] lastLevelPosition = null;
            for (Path externLvl : externLvls) {
                if (Files.isDirectory(externLvl)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(externLvl, entry -> {
                        String extension = FilenameUtils.getExtension(entry.toString()).toLowerCase(Locale.ROOT);
                        return extension.equals("rlv") || extension.equals("ini") || extension.equals("lvl") || extension.equals("dat");
                    })) {
                        for (Path lvl : stream) {
                            int[] levelPosition = GameController.addExternalLevel(lvl, null, false);
                            if (levelPosition != null) {
                                lastLevelPosition = levelPosition;
                            }
                        }
                    } catch (IOException ex) {
                    }
                } else {
                    int[] levelPosition = GameController.addExternalLevel(externLvl, null, false);
                    if (levelPosition != null) {
                        lastLevelPosition = levelPosition;
                    }
                }
            }
            if (lastLevelPosition != null) {
                refreshLevels();
                levelModel.reload();
                selectLevel(lastLevelPosition[0], lastLevelPosition[1], lastLevelPosition[2]);
                jButtonClearExternalLevels.setEnabled(GameController.externalLevelList.size() > 0);
            } else {
                JOptionPane.showMessageDialog(this, "No valid level files were loaded.", "Load Level", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButtonAddExternalLevelsActionPerformed

    private void jButtonClearExternalLevelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearExternalLevelsActionPerformed
        GameController.clearExternalLevelList();
        refreshLevels();
        levelModel.reload();
        jButtonClearExternalLevels.setEnabled(false);
    }//GEN-LAST:event_jButtonClearExternalLevelsActionPerformed
    
    private void jButtonGetMoreLevelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetMoreLevelsActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("https://www.lemmingsforums.net/index.php?msg=88514"));
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(jButtonGetMoreLevels, e, "Error", JOptionPane.ERROR_MESSAGE);
        }

        dispose();
    }//GEN-LAST:event_jButtonGetMoreLevelsActionPerformed
    
    private void jButtonGetMoreMusicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetMoreMusicActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("https://www.lemmingsforums.net/index.php?msg=108302"));
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(jButtonGetMoreMusic, e, "Error", JOptionPane.ERROR_MESSAGE);
        }

        dispose();
    }//GEN-LAST:event_jButtonGetMoreMusicActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        TreePath selPath = jTreeLevels.getSelectionPath();
        if (selPath == null) {
            dispose();
            return;
        }

        Object[] selPathArray = selPath.getPath();
        DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) selPathArray[selPathArray.length - 1];

        while (node.getChildCount() > 0) {
            node = (DefaultMutableTreeNode) node.getChildAt(0);
        }

        Object userObj = node.getUserObject();
        if (userObj instanceof LevelItem) {
            selectedLevel = (LevelItem) userObj;
        }

        dispose();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * Returns an int array consisting of the indices of the chosen levelIndex pack,
     * rating, and levelIndex. If no levelIndex was chosen, then null is returned.
     * @return int array or null
     */
    public int[] getSelectedLevel() {
        int[] retArray = null;
        if (selectedLevel != null) {
            retArray = new int[]{selectedLevel.levelPack,
                selectedLevel.rating, selectedLevel.levelIndex};
        }
        return retArray;
    }

    private void refreshLevels() {
        topNode.removeAllChildren();

        int levelPackCount = GameController.getLevelPackCount();
        levelPackPositionLookup = new int[levelPackCount];
        ratingPositionLookup = new int[levelPackCount][];
        levelPositionLookup = new int[levelPackCount][][];

        for (int i = 0, ia = 0; i < levelPackCount; i++) {
            LevelPack lp = GameController.getLevelPack(i);
            DefaultMutableTreeNode lpNode = new DefaultMutableTreeNode();

            List<String> ratings = lp.getRatings();
            ratingPositionLookup[i] = new int[ratings.size()];
            levelPositionLookup[i] = new int[ratings.size()][];

            int ja = 0;
            for (int j = 0; j < ratings.size(); j++) {
                String rating = ratings.get(j);
                DefaultMutableTreeNode ratingNode = new DefaultMutableTreeNode();

                List<String> levels = lp.getLevels(j);
                levelPositionLookup[i][j] = new int[levels.size()];
                int ka = 0;

                for (int k = 0; k < levels.size(); k++) {
                    String level = levels.get(k);
                    if (lp.getAllLevelsUnlocked() || Core.player.isAvailable(lp.getName(), rating, k)) {
                        LevelItem levelItem = new LevelItem(i, j, k, level,
                                                            Core.player.getLevelRecord(lp.getName(), rating, k).isCompleted());
                        DefaultMutableTreeNode levelNode = new DefaultMutableTreeNode(levelItem, false);
                        ratingNode.add(levelNode);
                        levelPositionLookup[i][j][k] = ka++;
                    } else {
                        levelPositionLookup[i][j][k] = -1;
                    }
                }
                ratingNode.setUserObject(rating);

                if (ratingNode.getChildCount() > 0) {
                    lpNode.add(ratingNode);
                    ratingPositionLookup[i][j] = ja++;
                } else {
                    ratingPositionLookup[i][j] = -1;
                }
            }
            lpNode.setUserObject(lp);

            if (lpNode.getChildCount() > 0) {
                topNode.add(lpNode);
                levelPackPositionLookup[i] = ia++;
            } else {
                levelPackPositionLookup[i] = -1;
            }
        }
    }
    
    // Returns true if the level pack or rating contains at least one incomplete level
    private boolean isAnyLevelIncomplete(LevelPack lp, int ratingIndex) {
        List<String> levels = lp.getLevels(ratingIndex);
        String rating = lp.getRatings().get(ratingIndex);
        for (int k = 0; k < levels.size(); k++) {
            if (lp.getAllLevelsUnlocked() || Core.player.isAvailable(lp.getName(), rating, k))
                if (!Core.player.getLevelRecord(lp.getName(), rating, k).isCompleted())
                    return true;
        }
        return false;
    }
    
    // Returns true if the level pack or rating contains at least one completed level
    private boolean isAnyLevelComplete(LevelPack lp, int ratingIndex) {
        List<String> levels = lp.getLevels(ratingIndex);
        String rating = lp.getRatings().get(ratingIndex);
        for (int k = 0; k < levels.size(); k++) {
            if (lp.getAllLevelsUnlocked() || Core.player.isAvailable(lp.getName(), rating, k))
                if (Core.player.getLevelRecord(lp.getName(), rating, k).isCompleted())
                    return true;
        }
        return false;
    }

    private void selectLevel(int lp, int rating, int level) {
        if (lp < 0 || lp >= levelPackPositionLookup.length
                || rating < 0 || rating >= ratingPositionLookup[lp].length
                || level < 0 || level >= levelPositionLookup[lp][rating].length) {
            return;
        }

        int newLp = levelPackPositionLookup[lp];
        int newRating = ratingPositionLookup[lp][rating];
        int newLevel = levelPositionLookup[lp][rating][level];

        if (newLp >= 0 && newRating >= 0 && newLevel >= 0) {
            Object[] selPathArray = new Object[4];
            TreeNode lpNode = topNode.getChildAt(newLp);
            TreeNode ratingNode = lpNode.getChildAt(newRating);
            TreeNode levelNode = ratingNode.getChildAt(newLevel);
            selPathArray[0] = topNode;
            selPathArray[1] = lpNode;
            selPathArray[2] = ratingNode;
            selPathArray[3] = levelNode;
            jTreeLevels.setSelectionPath(new TreePath(selPathArray));
        }

        fillInInfo();
        jTreeLevels.scrollPathToVisible(jTreeLevels.getSelectionPath());
    }

    private void selectCurrentLevel() {
        GameController.State state = GameController.getGameState();
        if (state == GameController.State.PREVIEW || state == GameController.State.LEVEL
                || state == GameController.State.LEVEL_END || state == GameController.State.POSTVIEW) {
            selectLevel(GameController.getCurLevelPackIdx(), GameController.getCurRating(), GameController.getCurLevelNumber());
        }
    }

    private void fillInInfo() {
        TreePath selPath = jTreeLevels.getSelectionPath();   
        if (selPath == null) return;

        Object[] selPathArray = selPath.getPath();
        if (selPathArray.length < 2) return;

        DefaultMutableTreeNode packNode = (DefaultMutableTreeNode) selPathArray[1];
        LevelPack pack = getPackFromTreeNode(packNode);

        boolean anyComplete = false;
        boolean anyIncomplete = false;
        for (int r = 0; r < pack.getRatings().size(); r++) {
            if (isAnyLevelComplete(pack, r))
                anyComplete = true;
            if (isAnyLevelIncomplete(pack, r))
                anyIncomplete = true;
            if (anyComplete && anyIncomplete)
                break;
        }
        boolean isFullyCompleted = anyComplete && !anyIncomplete;
        boolean isPartiallyCompleted = anyComplete && anyIncomplete;
        
        updatePackLogo(pack);
        updateFloaterLem(isFullyCompleted, isPartiallyCompleted);
        
        if (selPath != null && selPath.getPathCount() >= 4) {
            LevelItem lvlItem = (LevelItem) ((DefaultMutableTreeNode) selPath.getPath()[3]).getUserObject();
            LevelPack lvlPack = GameController.getLevelPack(lvlItem.levelPack);
            LevelInfo lvlInfo = lvlPack.getInfo(lvlItem.rating, lvlItem.levelIndex);
            LevelRecord lvlRecord = Core.player.getLevelRecord(lvlPack.getName(), lvlPack.getRatings().get(lvlItem.rating), lvlItem.levelIndex);
            jTextFieldAuthor.setText(lvlInfo.getAuthor());
            int numLemmings = lvlInfo.getNumLemmings();
            int numToRescue = lvlInfo.getNumToRescue();
            int timeLimit = lvlInfo.getTimeLimit();
            jTextFieldNumLemmings.setText(Integer.toString(numLemmings));
            if (GameController.isOptionEnabled(GameController.Option.NO_PERCENTAGES) || numLemmings > 100) {
                jTextFieldNumToRescue.setText(Integer.toString(numToRescue));
            } else {
                jTextFieldNumToRescue.setText(Integer.toString(numToRescue * 100 / numLemmings) + "%");
            }
            jTextFieldReleaseRate.setText(Integer.toString(lvlInfo.getReleaseRate()));
            if (timeLimit <= 0) {
                jTextFieldTimeLimit.setText("None");
            } else {
                jTextFieldTimeLimit.setText(String.format("%d:%02d", timeLimit / 60, timeLimit % 60));
            }
            jTextFieldNumClimbers.setText(ToolBox.intToString(lvlInfo.getNumClimbers(), true));
            jTextFieldNumFloaters.setText(ToolBox.intToString(lvlInfo.getNumFloaters(), true));
            jTextFieldNumBombers.setText(ToolBox.intToString(lvlInfo.getNumBombers(), true));
            jTextFieldNumBlockers.setText(ToolBox.intToString(lvlInfo.getNumBlockers(), true));
            jTextFieldNumBuilders.setText(ToolBox.intToString(lvlInfo.getNumBuilders(), true));
            jTextFieldNumBashers.setText(ToolBox.intToString(lvlInfo.getNumBashers(), true));
            jTextFieldNumMiners.setText(ToolBox.intToString(lvlInfo.getNumMiners(), true));
            jTextFieldNumDiggers.setText(ToolBox.intToString(lvlInfo.getNumDiggers(), true));
            if (lvlRecord.isCompleted()) {
                int lemmingsSaved = lvlRecord.getLemmingsSaved();
                int timeElapsed = lvlRecord.getTimeElapsed();
                if (GameController.isOptionEnabled(GameController.Option.NO_PERCENTAGES) || numLemmings > 100) {
                    jTextFieldLemmingsSaved.setText(Integer.toString(lvlRecord.getLemmingsSaved()));
                } else {
                    jTextFieldLemmingsSaved.setText(Integer.toString(lemmingsSaved * 100 / numLemmings) + "%");
                }
                jTextFieldSkillsUsed.setText(Integer.toString(lvlRecord.getSkillsUsed()));
                jTextFieldTimeElapsed.setText(String.format("%d:%02d", timeElapsed / 60, timeElapsed % 60));
                jTextFieldScore.setText(Integer.toString(lvlRecord.getScore()));
                updateFloaterLem(true, false);
            } else {
                jTextFieldLemmingsSaved.setText(StringUtils.EMPTY);
                jTextFieldSkillsUsed.setText(StringUtils.EMPTY);
                jTextFieldTimeElapsed.setText(StringUtils.EMPTY);
                jTextFieldScore.setText(StringUtils.EMPTY);
                updateFloaterLem(false, false);
            }
            jButtonOK.setText("Play Selected Level");
            jButtonOK.setEnabled(true);
        } else {
            jTextFieldAuthor.setText(StringUtils.EMPTY);
            jTextFieldNumLemmings.setText(StringUtils.EMPTY);
            jTextFieldNumToRescue.setText(StringUtils.EMPTY);
            jTextFieldReleaseRate.setText(StringUtils.EMPTY);
            jTextFieldTimeLimit.setText(StringUtils.EMPTY);
            jTextFieldNumClimbers.setText(StringUtils.EMPTY);
            jTextFieldNumFloaters.setText(StringUtils.EMPTY);
            jTextFieldNumBombers.setText(StringUtils.EMPTY);
            jTextFieldNumBlockers.setText(StringUtils.EMPTY);
            jTextFieldNumBuilders.setText(StringUtils.EMPTY);
            jTextFieldNumBashers.setText(StringUtils.EMPTY);
            jTextFieldNumMiners.setText(StringUtils.EMPTY);
            jTextFieldNumDiggers.setText(StringUtils.EMPTY);
            jTextFieldLemmingsSaved.setText(StringUtils.EMPTY);
            jTextFieldSkillsUsed.setText(StringUtils.EMPTY);
            jTextFieldTimeElapsed.setText(StringUtils.EMPTY);
            jTextFieldScore.setText(StringUtils.EMPTY);
            
            if (selPath != null && selPath.getPathCount() >= 3) {
                int ratingIndex = packNode.getIndex((DefaultMutableTreeNode)selPathArray[2]);
                anyComplete = isAnyLevelComplete(pack, ratingIndex);
                anyIncomplete = isAnyLevelIncomplete(pack, ratingIndex);
                updateFloaterLem(anyComplete && !anyIncomplete, anyComplete && anyIncomplete);
            	jButtonOK.setText("Play Selected Group");
            	jButtonOK.setEnabled(true);
            } else {
            	jButtonOK.setText("Play Selected Pack");
            	jButtonOK.setEnabled(true);
            }
        }
    }
    
    private static Image getScaledImage(Image original, int maxWidth, int maxHeight) {
        int width = original.getWidth(null);
        int height = original.getHeight(null);

        double scale = Math.min(
                (double) maxWidth / width,
                (double) maxHeight / height
        );

        int newW = (int) (width * scale);
        int newH = (int) (height * scale);

        Image scaled = original.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        
        return scaled;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddExternalLevels;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonClearExternalLevels;
    private javax.swing.JButton jButtonGetMoreLevels;
    private javax.swing.JButton jButtonGetMoreMusic;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabelAuthor;
    private javax.swing.JLabel jLabelLemmingsSaved;
    private javax.swing.JLabel jLabelNumBashers;
    private javax.swing.JLabel jLabelNumBlockers;
    private javax.swing.JLabel jLabelNumBombers;
    private javax.swing.JLabel jLabelNumBuilders;
    private javax.swing.JLabel jLabelNumClimbers;
    private javax.swing.JLabel jLabelNumDiggers;
    private javax.swing.JLabel jLabelNumFloaters;
    private javax.swing.JLabel jLabelNumLemmings;
    private javax.swing.JLabel jLabelNumMiners;
    private javax.swing.JLabel jLabelNumToRescue;
    private javax.swing.JLabel jLabelReleaseRate;
    private javax.swing.JLabel jLabelScore;
    private javax.swing.JLabel jLabelSkillsUsed;
    private javax.swing.JLabel jLabelTimeElapsed;
    private javax.swing.JLabel jLabelTimeLimit;
    private javax.swing.JPanel jPanelContent;
    private javax.swing.JPanel jPanelLevelInfo;
    private javax.swing.JPanel jPanelRecords;
    private javax.swing.JLabel jLabelFloaterImage;
    private javax.swing.JLabel jLabelLogoImage;
    private javax.swing.JScrollPane jScrollPaneLevels;
    private javax.swing.JSeparator jSeparatorSkills;
    private javax.swing.JSeparator jSeparatorRecords;
    private javax.swing.JTextField jTextFieldAuthor;
    private javax.swing.JTextField jTextFieldLemmingsSaved;
    private javax.swing.JTextField jTextFieldNumBashers;
    private javax.swing.JTextField jTextFieldNumBlockers;
    private javax.swing.JTextField jTextFieldNumBombers;
    private javax.swing.JTextField jTextFieldNumBuilders;
    private javax.swing.JTextField jTextFieldNumClimbers;
    private javax.swing.JTextField jTextFieldNumDiggers;
    private javax.swing.JTextField jTextFieldNumFloaters;
    private javax.swing.JTextField jTextFieldNumLemmings;
    private javax.swing.JTextField jTextFieldNumMiners;
    private javax.swing.JTextField jTextFieldNumToRescue;
    private javax.swing.JTextField jTextFieldReleaseRate;
    private javax.swing.JTextField jTextFieldScore;
    private javax.swing.JTextField jTextFieldSkillsUsed;
    private javax.swing.JTextField jTextFieldTimeElapsed;
    private javax.swing.JTextField jTextFieldTimeLimit;
    private javax.swing.JTree jTreeLevels;
    // End of variables declaration//GEN-END:variables
}
