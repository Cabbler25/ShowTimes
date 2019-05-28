import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;

@SuppressWarnings("serial")
public class MyScrollPane extends JScrollPane {

	public MyScrollPane(Component c) {
		getViewport().add(c);
		setBorder(null);
		getVerticalScrollBar().setUnitIncrement(50);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		getVerticalScrollBar().setUI(new MyScrollPaneUI());
		getHorizontalScrollBar().setUI(new MyScrollPaneUI());
		getVerticalScrollBar().setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, Color.black));
		getHorizontalScrollBar().setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, Color.black));

		JPanel invis = new JPanel();
		invis.setBackground(Color.black);
		invis.setPreferredSize(new Dimension(0, 0));
		invis.setMinimumSize(new Dimension(0, 0));
		invis.setMaximumSize(new Dimension(0, 0));
		setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, invis);
	}

	private class MyScrollPaneUI extends BasicScrollBarUI {
		@Override
		protected void configureScrollBarColors() {
			this.thumbColor = new Color(66, 66, 66);
			this.trackColor = Color.black;
		}

		// Gets rid of decrease button
		@Override
		protected JButton createDecreaseButton(int o) {
			JButton jbutton = new JButton();
			jbutton.setVisible(false);
			jbutton.setEnabled(false);
			jbutton.setPreferredSize(new Dimension(0, 0));
			jbutton.setMinimumSize(new Dimension(0, 0));
			jbutton.setMaximumSize(new Dimension(0, 0));
			return jbutton;
		}

		// Gets rid of increase button
		@Override
		protected JButton createIncreaseButton(int o) {
			JButton jbutton = new JButton();
			jbutton.setVisible(false);
			jbutton.setEnabled(false);
			jbutton.setPreferredSize(new Dimension(0, 0));
			jbutton.setMinimumSize(new Dimension(0, 0));
			jbutton.setMaximumSize(new Dimension(0, 0));
			return jbutton;
		}
	}
}
