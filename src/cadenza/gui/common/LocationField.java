package cadenza.gui.common;

import javax.swing.JTextField;

import common.swing.VerificationException;

import cadenza.core.LocationNumber;

@SuppressWarnings("serial")
public class LocationField extends JTextField {
  public LocationField() {
    this(LocationNumber.TEMP);
  }
  
  public LocationField(LocationNumber initial) {
    super(initial.toString(), 8);
  }
  
  public void verify() throws VerificationException {
    if (!LocationNumber.isValid(getText().trim()))
      throw new VerificationException("Invalid number, must be of the form (number?)(letter?)", this);
  }
  
  public void setLocationNumber(LocationNumber number) {
    setText(number.toString());
  }
  
  public LocationNumber getLocationNumber() {
    return LocationNumber.parse(getText().trim());
  }
}
