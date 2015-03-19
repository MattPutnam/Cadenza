package cadenza.gui.song;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import cadenza.core.Song;
import cadenza.gui.common.LocationField;

import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class SongEditDialog extends OKCancelDialog {
  final Song _song;
  
  LocationField _numberField;
  JTextField _nameField;
  
  public SongEditDialog(Component parent, Song song) {
    super(parent);
    _song = song;
  }
  
  @Override
  protected JComponent buildContent() {
    _numberField = new LocationField();
    _nameField = new JTextField(30);
    
    final Box box = Box.createHorizontalBox();
    box.add(new JLabel(" Number: "));
    box.add(_numberField);
    box.add(new JLabel(" Name: "));
    box.add(_nameField);
    
    return box;
  }
  
  @Override
  protected void initialize() {
    if (_song.number != null)
      _numberField.setLocationNumber(_song.number);
    if (_song.name != null)
      _nameField.setText(_song.name);
  }
  
  @Override
  protected String declareTitle() {
    return _song.name.isEmpty() ? "Create Song" : "Edit Song";
  }
  
  @Override
  protected void verify() throws VerificationException {
    _numberField.verify();
    if (_nameField.getText().trim().isEmpty())
      throw new VerificationException("Please enter a song name", _nameField);
  }
  
  @Override
  protected void takeActionOnOK() {
    _song.number = _numberField.getLocationNumber();
    _song.name = _nameField.getText().trim();
  }
}