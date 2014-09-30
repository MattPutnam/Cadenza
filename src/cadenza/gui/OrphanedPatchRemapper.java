package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import cadenza.core.CadenzaData;
import cadenza.core.Patch;
import cadenza.core.Synthesizer;
import cadenza.gui.patch.PatchPickerDialog;
import cadenza.synths.Synthesizers;

import common.Utils;
import common.swing.BlockingTask;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class OrphanedPatchRemapper extends OKCancelDialog {
	private static final String MESSAGE = "The following patches were left " +
			"orphaned by the synthesizer edit process.\nDouble-click each row to select a new patch.";
	private static final int MAX_SUGGESTIONS = 8;
	private static final int DISTANCE_THRESHOLD = 3;
	
	private final CadenzaData _data;
	private final List<Patch> _orphans;
	private final List<Synthesizer> _synthesizers;
	private final Map<Patch, Patch> _remapping;
	private final Map<Patch, List<Patch>> _suggestions;
	
	private JList<Patch> _remappingList;
	
	public OrphanedPatchRemapper(Component parent, CadenzaData data,
			List<Patch> orphans, List<Synthesizer> synthesizers) {
		super(parent);
		_data = data;
		_orphans = orphans;
		_synthesizers = synthesizers;
		_remapping = new HashMap<>();
		_suggestions = new HashMap<>();
		
		new BlockingTask(this, new Runnable() {
		  @Override
		  public void run() {
		    _suggestions.putAll(buildSuggestions());
		  }
		}).start();
	}
	
	private Map<Patch, List<Patch>> buildSuggestions() {
		final Map<Patch, List<Patch>> result = new HashMap<>();
		
		for (final Patch orphan : _orphans) {
			final String orphanName = orphan.name.toLowerCase();
			final SortedMap<Integer, Patch> suggestions = new TreeMap<>();
			
			for (final Synthesizer synth : _synthesizers) {
				for (final Patch sPatch : Synthesizers.loadPatches(synth)) {
					final String sPatchName = sPatch.name.toLowerCase();
					final int distance = Utils.levenshteinDistance(orphanName, sPatchName);
					
					if (distance <= DISTANCE_THRESHOLD || orphanName.contains(sPatchName) || sPatchName.contains(orphanName)) {
						suggestions.put(Integer.valueOf(distance), sPatch);
						if (suggestions.size() > MAX_SUGGESTIONS)
							suggestions.remove(suggestions.lastKey());
					}
				}
			}
			
			result.put(orphan, new ArrayList<>(suggestions.values()));
		}
		
		return result;
	}

	@Override
	protected JComponent buildContent() {
	  _remappingList = new JList<>(_orphans.toArray(new Patch[_orphans.size()]));
		_remappingList.setCellRenderer(new RemapRenderer());
		_remappingList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					final Patch patch = _remappingList.getSelectedValue();
					final PatchPickerDialog dialog = new PatchPickerDialog(OrphanedPatchRemapper.this,
							_synthesizers, _suggestions.get(patch));
					dialog.showDialog();
					if (dialog.okPressed()) {
						_remapping.put(patch, dialog.getSelectedPatch());
						revalidate();
						repaint();
					}
				}
			}
		});
		_remappingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		final JTextArea messageArea = new JTextArea(MESSAGE);
		messageArea.setEditable(false);
		messageArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 32, 8));
		
		final JPanel content = new JPanel(new BorderLayout());
		content.add(messageArea, BorderLayout.NORTH);
		content.add(new JScrollPane(_remappingList), BorderLayout.CENTER);
		
		return content;
	}

	@Override
	protected String declareTitle() {
		return "Remap Orphaned Patches";
	}

	@Override
	protected void verify() throws VerificationException {
		if (_remapping.size() < _orphans.size())
			throw new VerificationException("Please select a new patch for all orphans");
	}
	
	@Override
	protected void takeActionOnOK() {
		for (final Patch patch : _orphans) {
			patch.copyFrom(_remapping.get(patch), false);
			_data.patches.notifyChange(patch);
		}
		
		for (int i = 0; i < _data.cues.size(); ++i) {
			_data.cues.notifyChange(i);
		}
		
		_data.synthesizers.clear();
		_data.synthesizers.addAll(_synthesizers);
	}
	
	private class RemapRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected, boolean cellHasFocus) {
			
			final JLabel label = (JLabel) super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);
			
			final Patch patch = (Patch) value;
			final Patch remapped = _remapping.get(patch);
			if (remapped == null) {
				label.setText("<html>" + patch.name + " &rarr; [none selected]</html>");
			} else {
				label.setText("<html>" + patch.name + " &rarr; " + remapped.name + "</html>");
			}
			
			return label;
		}
	}

}
