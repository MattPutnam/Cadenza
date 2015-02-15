package cadenza.gui.song;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.core.Song;

import common.swing.SwingUtils;
import common.swing.VerificationException;

@SuppressWarnings("serial")
public class SongPanel extends JPanel {
  private final List<Song> _songs;
  
  private final JLabel _label;
  private final JComboBox<Song> _songCombo;
  
  public SongPanel(List<Song> songs, boolean allowAdd) {
    _songs = songs;
    
    _songCombo = new JComboBox<>(_songs.toArray(new Song[_songs.size()]));
    _label = new JLabel("Song: ");
    final JButton addButton = new JButton(new AddSongAction());
    
    final Box box = Box.createHorizontalBox();
    box.add(_label);
    box.add(_songCombo);
    if (allowAdd)
      box.add(addButton);
    
    setLayout(new BorderLayout());
    add(box, BorderLayout.CENTER);
    SwingUtils.freezeHeight(this, box.getPreferredSize().height);
  }
  
  private class AddSongAction extends AbstractAction {
    public AddSongAction() {
      super("Add new song");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final Song newSong = new Song("", "");
      final SongEditDialog dialog = new SongEditDialog(SongPanel.this, newSong);
      dialog.showDialog();
      if (dialog.okPressed()) {
        _songs.add(newSong);
        Collections.sort(_songs);
        _songCombo.setModel(new DefaultComboBoxModel<>(_songs.toArray(new Song[_songs.size()])));
        _songCombo.setSelectedItem(newSong);
      }
    }
  }
  
  public void setSelectedSong(Song song) {
    _songCombo.setSelectedItem(song);
  }
  
  public Song getSelectedSong() {
    return (Song) _songCombo.getSelectedItem();
  }
  
  public void verify() throws VerificationException {
    if (_songCombo.getSelectedIndex() == -1)
      throw new VerificationException("Please select a song");
  }
  
  @Override
  public void setBackground(Color bg) {
    super.setBackground(bg);
    if (_label != null) // UI manager calls this before constructor
      _label.setBackground(bg);
  }
  
  @Override
  public void setForeground(Color fg) {
    super.setForeground(fg);
    if (_label != null) // UI manager calls this before constructor
      _label.setForeground(fg);
  }
  
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    _label.setEnabled(enabled);
    _songCombo.setEnabled(enabled);
  }
}
