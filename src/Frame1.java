import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

public class Frame1 {

	private JFrame frame;
	private JPanel cardPanel;
	private SearchPanel searchPanel;
	private JTextField txtSearch;
	private MyShowsPanel myShowsPanel;
	private JButton btnSubmitSearch;
	private MyScrollPane scroll;
	private JButton btnSearchResults;
	private Timer searchSleepTimer;
	private JPanel myShowsBtnPanel;
	private JPanel searchResultsBtnPanel;
	private JButton btnMyShows;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				Frame1 window = new Frame1();
				window.frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Frame1() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Shows");
		frame.setLocation(200, 200);
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			System.out.println("Could not set look and feel");
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				myShowsPanel.saveShowComponents();
			}
		});

		JPanel contentPanel = new JPanel();
		frame.getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JPanel navigationPanel = new JPanel();
		navigationPanel.setBackground(Color.black);
		navigationPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		contentPanel.add(navigationPanel, BorderLayout.NORTH);
		navigationPanel.setLayout(new BorderLayout(0, 0));

		JPanel viewsPanel = new JPanel();
		viewsPanel.setBackground(Color.black);
		navigationPanel.add(viewsPanel, BorderLayout.WEST);

		myShowsBtnPanel = new JPanel();
		myShowsBtnPanel.setBackground(Color.BLACK);
		myShowsBtnPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		viewsPanel.add(myShowsBtnPanel);

		btnMyShows = new JButton("My Shows");
		myShowsBtnPanel.add(btnMyShows);
		btnMyShows.setForeground(new Color(80, 80, 80));
		btnMyShows.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		btnMyShows.setContentAreaFilled(false);
		btnMyShows.addActionListener(arg0 -> {
			if (searchPanel.isShowing()) {
				CardLayout cl = (CardLayout) cardPanel.getLayout();
				cl.show(cardPanel, "showsPanel");
				btnSearchResults.setForeground(Color.white);
				btnMyShows.setForeground(new Color(80, 80, 80));
			}
			System.out.println(frame.getSize());
			System.out.println(cardPanel.getSize());
		});
		btnMyShows.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!myShowsPanel.isShowing()) {
					btnMyShows.setForeground(new Color(80, 80, 80));
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!myShowsPanel.isShowing()) {
					btnMyShows.setForeground(Color.white);
				}
			}
		});

		searchResultsBtnPanel = new JPanel();
		searchResultsBtnPanel.setBackground(Color.BLACK);
		searchResultsBtnPanel.setBorder(new MatteBorder(0, 1, 0, 0, Color.WHITE));
		viewsPanel.add(searchResultsBtnPanel);

		btnSearchResults = new JButton("Search Results");
		searchResultsBtnPanel.add(btnSearchResults);
		btnSearchResults.setForeground(Color.white);
		btnSearchResults.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		btnSearchResults.setContentAreaFilled(false);
		btnSearchResults.addActionListener(arg0 -> {
			if (myShowsPanel.isShowing()) {
				CardLayout cl = (CardLayout) cardPanel.getLayout();
				cl.show(cardPanel, "searchPanel");
				btnMyShows.setForeground(Color.white);
				btnSearchResults.setForeground(new Color(80, 80, 80));
			}

			System.out.println(myShowsPanel.getSize());
		});
		btnSearchResults.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!searchPanel.isShowing()) {
					btnSearchResults.setForeground(new Color(80, 80, 80));
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!searchPanel.isShowing()) {
					btnSearchResults.setForeground(Color.white);
				}
			}
		});

		JPanel searchBarPanel = new JPanel();
		searchBarPanel.setBackground(Color.black);
		navigationPanel.add(searchBarPanel, BorderLayout.EAST);
		searchBarPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 15));
		JButton btnShowSearch = new JButton();
		try {
			btnShowSearch.setIcon(new ImageIcon(getSearchIcon()));
			btnShowSearch.setContentAreaFilled(false);
		} catch (Exception e) {
			System.out.println("Search icon image not found");
		}
		btnShowSearch.addActionListener(e -> {
			if (!txtSearch.isVisible()) {
				txtSearch.setVisible(true);
				txtSearch.requestFocus();
				btnSubmitSearch.setVisible(true);
				searchSleepTimer.restart();
				try {
					btnShowSearch.setIcon(new ImageIcon(getSearchIcon()));
				} catch (IOException e11) {
					System.out.println("Get default search icon err");
					e11.printStackTrace();
				}
			} else {
				searchSleepTimer.stop();
				txtSearch.setVisible(false);
				btnSubmitSearch.setVisible(false);
				try {
					btnShowSearch.setIcon(new ImageIcon(getSearchIcon()));
				} catch (IOException e12) {
					System.out.println("Get default search icon err");
					e12.printStackTrace();
				}
			}

		});
		btnShowSearch.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				BufferedImage bi = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
				Graphics g = bi.createGraphics();
				btnShowSearch.getIcon().paintIcon(null, g, 0, 0);
				g.dispose();
				RescaleOp op = new RescaleOp(.5f, 0, null);
				bi = op.filter(bi, null);
				btnShowSearch.setIcon(new ImageIcon(bi));
				bi.flush();
				bi = null;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				try {
					btnShowSearch.setIcon(new ImageIcon(getSearchIcon()));
				} catch (IOException e1) {
					System.out.println("Get default search icon err");
					e1.printStackTrace();
				}
			}
		});

		JButton btnUpdateButton = new JButton();
		searchBarPanel.add(btnUpdateButton);
		try {
			btnUpdateButton.setIcon(new ImageIcon(getRefreshIcon()));
			btnUpdateButton.setContentAreaFilled(false);
		} catch (Exception e) {
			System.out.println("Refresh icon image not found");
		}
		btnUpdateButton.setContentAreaFilled(false);
		btnUpdateButton.addActionListener(arg0 -> {

			CardLayout cl = (CardLayout) cardPanel.getLayout();
			if (!myShowsPanel.isShowing()) {
				cl.show(cardPanel, "showsPanel");
				btnSearchResults.setForeground(Color.white);
				btnMyShows.setForeground(new Color(80, 80, 80));
			}
			myShowsPanel.updateAllShows();
		});
		btnUpdateButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				BufferedImage bi = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
				Graphics g = bi.createGraphics();
				btnUpdateButton.getIcon().paintIcon(null, g, 0, 0);
				g.dispose();
				RescaleOp op = new RescaleOp(.5f, 0, null);
				bi = op.filter(bi, null);
				btnUpdateButton.setIcon(new ImageIcon(bi));
				bi.flush();
				bi = null;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				try {
					btnUpdateButton.setIcon(new ImageIcon(getRefreshIcon()));
				} catch (IOException e1) {
					System.out.println("Get default search icon err");
					e1.printStackTrace();
				}
			}
		});
		searchBarPanel.add(btnShowSearch);

		txtSearch = new JTextField();
		txtSearch.setVisible(false);
		txtSearch.setFont(new Font("Segoe UI", Font.ITALIC, txtSearch.getFont().getSize()));
		txtSearch.setForeground(Color.gray);
		txtSearch.setText("Search IMDB");
		searchSleepTimer = new Timer(15000, e -> {
			if (!txtSearch.isFocusOwner()) {
				txtSearch.setVisible(false);
				btnSubmitSearch.setVisible(false);
				btnShowSearch.setVisible(true);
				searchSleepTimer.stop();
			}
		});
		txtSearch.addActionListener(e -> newSearch());
		txtSearch.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				searchSleepTimer.restart();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO: BUG HERE! UNWANTED BEHAVIOR WHEN ENTERING SEARCH BOX AFTER PRESSING GO BUTTON
				if (e.getOppositeComponent() != btnSubmitSearch) {
					txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
					txtSearch.setForeground(Color.black);
					txtSearch.setText("");
					searchSleepTimer.stop();
				}
			}
		});
		txtSearch.setHorizontalAlignment(SwingConstants.LEFT);
		searchBarPanel.add(txtSearch);
		txtSearch.setColumns(17);

		btnSubmitSearch = new JButton("Go");
		btnSubmitSearch.setVisible(false);
		btnSubmitSearch.setContentAreaFilled(false);
		btnSubmitSearch.setForeground(new Color(146, 149, 156));
		btnSubmitSearch.setFont(new Font("Segoe UI", Font.BOLD, 15));
		btnSubmitSearch.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 15));
		btnSubmitSearch.addActionListener(arg0 -> {
			btnSubmitSearch.setForeground(new Color(90, 90, 90));
			newSearch();
		});
		btnSubmitSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				btnSubmitSearch.setForeground(new Color(80, 80, 80));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				btnSubmitSearch.setForeground(new Color(146, 149, 156));
			}
		});
		searchBarPanel.add(btnSubmitSearch);

		cardPanel = new JPanel();
		contentPanel.add(cardPanel);
		cardPanel.setLayout(new CardLayout());

		myShowsPanel = new MyShowsPanel();
		scroll = new MyScrollPane(myShowsPanel);
		scroll.getViewport().addMouseWheelListener(new ScrollByComponent(scroll, myShowsPanel,
				myShowsPanel.getMaxRowAmount(), 2, myShowsPanel.getComponents(), myShowsPanel.getGridBagInsets()));
		cardPanel.add(scroll, "showsPanel");

		searchPanel = new SearchPanel(myShowsPanel);
		MyScrollPane scrollTwo = new MyScrollPane(searchPanel);
		scrollTwo.getViewport().addMouseWheelListener(new ScrollByComponent(scrollTwo, searchPanel,
				searchPanel.getMaxRowAmount(), 2, searchPanel.getComponents(), searchPanel.getGridBagInsets()));
		cardPanel.add(scrollTwo, "searchPanel");

		// Have to subtract 15 off height for perfection, dunno why
		cardPanel.setPreferredSize(
				new Dimension(MyShowsPanel.getOptimalDisplayWidth(), MyShowsPanel.getOptimalDisplayHeight() - 15));

		//configureShowComponentUIBehavior();
		frame.repaint();
		frame.revalidate();
		frame.pack();
	}

	private void newSearch() {

		String parameters = txtSearch.getText();

		if (parameters == null || parameters.equalsIgnoreCase("Search IMDB") || parameters.equals("")) {
			return;
		}

		String url = "http://www.imdb.com/find?ref_=nv_sr_fn&q=" + parameters + "&s=all";
		((CardLayout) cardPanel.getLayout()).show(cardPanel, "searchPanel");
		btnSearchResults.setForeground(new Color(80, 80, 80));
		btnMyShows.setForeground(Color.white);
		searchPanel.imdbSearch(url, parameters);
	}

	/*private void configureShowComponentUIBehavior() {
		long eventMask = AWTEvent.MOUSE_EVENT_MASK;
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent e) {
				MouseEvent ev = (MouseEvent) e;
				if (ev.getID() == MouseEvent.MOUSE_ENTERED || ev.getID() == MouseEvent.MOUSE_EXITED) {
					Component c = ev.getComponent();
					boolean flag = (c instanceof SearchResultComponent || c instanceof MyShowComponent || c == null)
							? true : false;
					while (!flag) {
						c = c.getParent();
						if (c == null || c instanceof SearchPanel || c instanceof MyShowsPanel)
							flag = true;
						if (c instanceof SearchResultComponent || c instanceof MyShowComponent)
							flag = true;
					}
					if (c instanceof SearchResultComponent) {
						SearchResultComponent comp = (SearchResultComponent) c;
						if (!comp.isMouseOverPanelVisible()) {
							comp.showMouseOverPanel(true);
							for (Component thisComp : searchPanel.getComponents())
								if (thisComp instanceof SearchResultComponent) {
									SearchResultComponent newComp = (SearchResultComponent) thisComp;
									if (newComp != comp && newComp.isMouseOverPanelVisible()) {
										newComp.showMouseOverPanel(false);
										newComp.startMaskFadeOutAnimation();
									}
								}
							lastSelectedSearchComponent = comp;
						}
					} else if (c instanceof MyShowComponent) {
						MyShowComponent comp = (MyShowComponent) c;
						if (!comp.isMouseOverPanelVisible()) {
							comp.showMouseOverPanel(true);
							for (MyShowComponent thisC : myShowsPanel.getAllShowComponents())
								if (thisC.isMouseOverPanelVisible() && thisC != comp) {
									thisC.showMouseOverPanel(false);
									thisC.startMaskFadeOutAnimation();
								}
							lastSelectedShowComponent = comp;
						}
					} else {
						if (lastSelectedSearchComponent != null
								&& lastSelectedSearchComponent.isMouseOverPanelVisible()) {
							lastSelectedSearchComponent.showMouseOverPanel(false);
							lastSelectedSearchComponent.startMaskFadeOutAnimation();
						}
						if (lastSelectedShowComponent != null && lastSelectedShowComponent.isMouseOverPanelVisible()) {
							lastSelectedShowComponent.showMouseOverPanel(false);
							lastSelectedShowComponent.startMaskFadeOutAnimation();
						}
					}
				} else
					return;
			}
		}, eventMask);
	}*/

	public static BufferedImage getDefaultShowIcon() throws IOException {
		return ImageIO.read(new File("./resources/defaultimgs/defaultmovieimg.png"));
	}

	public static BufferedImage getSearchIcon() throws IOException {
		return ImageIO.read(new File("./resources/defaultimgs/search-icon.png"));
	}

	public static BufferedImage getRefreshIcon() throws IOException {
		return ImageIO.read(new File("./resources/defaultimgs/refresh-icon.png"));
	}
}
