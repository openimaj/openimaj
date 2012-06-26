package org.openimaj.ide.eclipseplugin;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.openimaj.image.FImage;

/**
 * A factory for providing image views in the debugger
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ImageDetailPaneFactory implements IDetailPaneFactory {

	@Override
	public IDetailPane createDetailPane(String paneID) {
		if (paneID.equals(ImageDetailPane.ID)) {
			return new ImageDetailPane();
		}
		
		return null;
	}

	@Override
	public String getDefaultDetailPane(IStructuredSelection arg0) {
		return null;
	}

	@Override
	public String getDetailPaneDescription(String paneID) {
		if (paneID.equals(ImageDetailPane.ID)) {
			return ImageDetailPane.DESCRIPTION;
		}

		return null;
	}

	@Override
	public String getDetailPaneName(String paneID) {
		if (paneID.equals(ImageDetailPane.ID)) {
			return ImageDetailPane.NAME;
		}

		return null;
	}

	@Override
	public Set<String> getDetailPaneTypes(IStructuredSelection selection) {
		IJavaVariable var = (IJavaVariable) selection.getFirstElement();
		Set<String> panes = new HashSet<String>();
		try {
			if (var.getJavaType().getName().equals(FImage.class.getName())) {
				panes.add(ImageDetailPane.ID);
			}
		} catch (DebugException e) {
			e.printStackTrace();
		}
		
		return panes;
	}
}
