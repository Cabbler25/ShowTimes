import java.awt.Component;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

public class ScrollByComponent implements MouseWheelListener {
	private JScrollPane scrollPane;
	private JPanel scrollablePanel;
	private Component[] allScrollableComponents;
	private int maxItemsPerRow, maxItemsPerColumn;
	private MouseWheelListener[] defaultMouseWheelListener;
	private Insets scrollComponentInsets = null;

	public ScrollByComponent(JScrollPane scrollPane, JPanel scrollablePanel, int maxItemsPerRow, int maxItemsPerColumn,
			Component[] allScrollableComponents) {
		this.scrollPane = scrollPane;
		this.scrollablePanel = scrollablePanel;
		this.maxItemsPerRow = maxItemsPerRow;
		this.maxItemsPerColumn = maxItemsPerColumn;
		this.allScrollableComponents = allScrollableComponents;
		defaultMouseWheelListener = scrollPane.getMouseWheelListeners();
	}

	public ScrollByComponent(JScrollPane scrollPane, JPanel scrollablePanel, int maxItemsPerRow, int maxItemsPerColumn,
			Component[] allScrollableComponents, Insets componentInsets) {
		this.scrollPane = scrollPane;
		this.scrollablePanel = scrollablePanel;
		this.maxItemsPerRow = maxItemsPerRow;
		this.maxItemsPerColumn = maxItemsPerColumn;
		this.allScrollableComponents = allScrollableComponents;
		defaultMouseWheelListener = scrollPane.getMouseWheelListeners();
		scrollComponentInsets = componentInsets;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

		// Mouse scroll down
		if (e.getWheelRotation() > 0) { 
			if (scrollPane.getVerticalScrollBar().getValue()
					+ scrollPane.getVerticalScrollBar().getVisibleAmount() >= scrollPane.getVerticalScrollBar()
							.getMaximum()) 
				return;
			Component scrollToComponent = null;
			JViewport scrollViewPort = scrollPane.getViewport();
			if (allScrollableComponents.length == 0)
				return;
			// Check if viewport is too small or too large for component
			// scroll to make sense
			if (scrollViewPort.getHeight() < allScrollableComponents[0].getHeight()
					|| scrollViewPort.getHeight() > allScrollableComponents[0].getHeight()
							* (maxItemsPerColumn + maxItemsPerColumn * 0.25)) {
				if (defaultMouseWheelListener.length > 0)
					defaultMouseWheelListener[0].mouseWheelMoved(e);
				return;
			} else {
				Rectangle viewPortRectangle = scrollViewPort.getViewRect();
				int index = 0;
				// Get component currently in viewport
				if (allScrollableComponents.length > maxItemsPerRow) {
					for (int i = 0; i < allScrollableComponents.length; i += maxItemsPerRow) {
						if (viewPortRectangle.contains(allScrollableComponents[i].getLocation())) {
							scrollToComponent = allScrollableComponents[i];
							index = i;
							break;
						}
					}
					if (scrollToComponent == null) {
						defaultMouseWheelListener[0].mouseWheelMoved(e);
						System.out.println("No component found");
						return;
					}
					// If at bottom
					if (allScrollableComponents.length < index + 1 + maxItemsPerRow * maxItemsPerColumn) {
						System.out.println("Hit bottom");
						scrollablePanel.scrollRectToVisible(
								new Rectangle(scrollToComponent.getLocation().x, scrollablePanel.getHeight(), 1, 1));
						return;
					}
					// If there is another row of panels
					else if (allScrollableComponents.length > index + 1 + maxItemsPerRow) {
						scrollToComponent = allScrollableComponents[index + maxItemsPerRow];
					}

					int y;
					if (scrollComponentInsets != null)
						y = scrollToComponent.getY() + scrollToComponent.getHeight()
								+ scrollComponentInsets.bottom - (scrollViewPort.getHeight() / maxItemsPerColumn);
					else
						y = scrollToComponent.getY() + scrollToComponent.getHeight()
								- (scrollViewPort.getHeight() / maxItemsPerColumn);
	
					if (y > scrollToComponent.getY())
						y = scrollToComponent.getY();
					
					scrollablePanel.scrollRectToVisible(new Rectangle((int) scrollViewPort.getViewRect().getMinX(), y,
							scrollViewPort.getWidth(), scrollViewPort.getHeight()));
				}
			}
		}
		// Mouse scroll up
		else if (e.getWheelRotation() < 0) {
			if (scrollPane.getVerticalScrollBar().getValue() <= scrollPane.getVerticalScrollBar().getMinimum()) 
				return;
			Component scrollToComponent = null;
			JViewport scrollViewPort = scrollPane.getViewport();
			if (allScrollableComponents.length == 0)
				return;
			if (scrollViewPort.getHeight() < allScrollableComponents[0].getHeight()
					|| scrollViewPort.getHeight() > allScrollableComponents[0].getHeight()
							* (maxItemsPerColumn + maxItemsPerColumn * 0.25)) {
				if (defaultMouseWheelListener.length > 0)
					defaultMouseWheelListener[0].mouseWheelMoved(e);
				return;
			} else {
				Rectangle viewRectangle = scrollViewPort.getViewRect();
				int index = 0;
				for (int i = 0; i < allScrollableComponents.length; i += maxItemsPerRow) {
					if (viewRectangle.contains(allScrollableComponents[i].getLocation())) {
						scrollToComponent = allScrollableComponents[i];
						index = i;
						break;
					}
				}

				if (scrollToComponent == null) {
					defaultMouseWheelListener[0].mouseWheelMoved(e);
					return;
				}

				if (index != 0 && index - maxItemsPerRow > -1) {
					scrollToComponent = allScrollableComponents[index - maxItemsPerRow];
				} else {
					scrollablePanel.scrollRectToVisible(new Rectangle(scrollToComponent.getLocation().x, 0, 1, 1));
					return;
				}

				int y;
				if (scrollComponentInsets != null)
					y = scrollToComponent.getY() + scrollToComponent.getHeight() + scrollComponentInsets.bottom
							- (scrollViewPort.getHeight() / maxItemsPerColumn);
				else
					y = scrollToComponent.getY() + scrollToComponent.getHeight()
							- (scrollViewPort.getHeight() / maxItemsPerColumn);

				if (y > scrollToComponent.getY())
					y = scrollToComponent.getY();
				
				scrollablePanel.scrollRectToVisible(new Rectangle((int) scrollViewPort.getViewRect().getMinX(), y,
						scrollViewPort.getWidth(), scrollViewPort.getHeight()));
			}
		}
	}
}
