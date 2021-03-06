package cadenza.gui.song;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.core.LocationNumber;
import cadenza.core.Song;

import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class SongPanel extends JPanel {
  private final List<Song> _songs;
  
  private final JLabel _label;
  private final JComboBox<Song> _songCombo;
  
  public SongPanel(List<Song> songs, boolean allowAdd) {
    _songs = songs;
    
    _songCombo = new JComboBox<>(_songs.toArray(new Song[_songs.size()]));
    _label = new JLabel("Song: ");
    final JButton addButton = SwingUtils.button("Add new song", e -> {
      final Song newSong = new Song(LocationNumber.TEMP, "");
      OKCancelDialog.showDialog(new SongEditDialog(SongPanel.this, newSong), dialog -> {
        _songs.add(newSong);
        _songs.sort(null);
        _songCombo.setModel(new DefaultComboBoxModel<>(_songs.toArray(new Song[_songs.size()])));
        _songCombo.setSelectedItem(newSong);
      });
    });
    
    final Box box = Box.createHorizontalBox();
    box.add(_label);
    box.add(_songCombo);
    if (allowAdd)
      box.add(addButton);
    
    setLayout(new BorderLayout());
    add(box, BorderLayout.CENTER);
    SwingUtils.freezeHeight(this, box.getPreferredSize().height);
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
