package cadenza.gui.common;

import java.awt.Component;
import java.util.List;

import cadenza.gui.ImageStore;

import common.swing.table.ListTable;

@SuppressWarnings("serial")
public abstract class CadenzaTable<T> extends ListTable<T> {
	public CadenzaTable(List<T> list, boolean allowEdit, boolean allowArrows) {
		super(list, allowEdit, allowArrows,
				ImageStore.ADD,
				ImageStore.EDIT,
				ImageStore.DELETE,
				ImageStore.UP_ARROW,
				ImageStore.DOWN_ARROW);
	}
	
	public CadenzaTable(List<T> list, boolean allowEdit, boolean allowArrows, String label, Component... extras) {
		super(list, allowEdit, allowArrows, label,
				ImageStore.ADD,
				ImageStore.EDIT,
				ImageStore.DELETE,
				ImageStore.UP_ARROW,
				ImageStore.DOWN_ARROW,
				extras);
	}

}
