import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@SuppressWarnings("serial")
public class SearchResultComponent extends JLayeredPane {
	private final static int HOVER_OVER_DELAY = 300;
	private final static int PANEL_WIDTH = 200;
	private final static int PANEL_HEIGHT = 300;
	private final static int PANEL_BORDER = 10;
	private String url;
	private String title;
	private String episodeGuideURL;
	private Timer hoverDelayTimer, checkMouseTimer;

	private JLabel lblPosterImage = new JLabel();
	private JLabel lblAddShow;
	private JPanel mouseOverPanel;
	private JLabel lblRating;
	private JLabel lblTitle;

	private TransparentMask mask;

	private MyShowsPanel shows;

	public SearchResultComponent() {
	}

	public SearchResultComponent(String urlIn, MyShowsPanel showPanel) {
		hoverDelayTimer = new Timer(HOVER_OVER_DELAY, e -> {
			Point currentMousePos = MouseInfo.getPointerInfo().getLocation();
			Point compPoint = getLocationOnScreen();
			Rectangle rec = new Rectangle((int) compPoint.getX(), (int) compPoint.getY(), getWidth(), getHeight());
			if (rec.contains(currentMousePos)) {
				startMaskFadeInAnimation();
				mouseOverPanel.setVisible(true);
				checkMouseTimer.restart();
			} else {
				mouseOverPanel.setVisible(false);
				mask.stopAnimation();
			}
			hoverDelayTimer.stop();
		});
		checkMouseTimer = new Timer(10, e -> {
			if (isShowing()) {
				Point currentMousePos = MouseInfo.getPointerInfo().getLocation();
				Point compPoint = getLocationOnScreen();
				Rectangle r = new Rectangle((int) compPoint.getX(), (int) compPoint.getY(), getWidth(), getHeight());
				if (!r.contains(currentMousePos)) {
					if (mouseOverPanel.isVisible()) {
						startMaskFadeOutAnimation();
						mouseOverPanel.setVisible(false);
					}
					checkMouseTimer.stop();
				}
			} else {
				mouseOverPanel.setVisible(false);
				checkMouseTimer.stop();
			}
		});

		shows = showPanel;
		url = urlIn;
		setOpaque(true);
		setBackground(new Color(66, 66, 66));
		setPreferredSize(new Dimension(PANEL_WIDTH + PANEL_BORDER, PANEL_HEIGHT + PANEL_BORDER));

		lblPosterImage = new JLabel();
		lblPosterImage.setBounds(PANEL_BORDER / 2, PANEL_BORDER / 2, PANEL_WIDTH, PANEL_HEIGHT);
		lblPosterImage.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		lblPosterImage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				hoverDelayTimer.restart();
			}
		});
		add(lblPosterImage, new Integer(1));

		mask = new TransparentMask(0, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
		mask.setBounds(PANEL_BORDER / 2, PANEL_BORDER / 2, PANEL_WIDTH, PANEL_HEIGHT);
		add(mask, new Integer(2));

		mouseOverPanel = new JPanel();
		mouseOverPanel.setLayout(new BorderLayout());
		mouseOverPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
		mouseOverPanel.setVisible(false);
		mouseOverPanel.setOpaque(false);
		mouseOverPanel.setBounds(PANEL_BORDER / 2, PANEL_BORDER / 2, PANEL_WIDTH, PANEL_HEIGHT);

		/* Create hover title label */
		lblTitle = new JLabel("<html><center>" + "Title" + "</center></html>", SwingConstants.CENTER);
		lblTitle.setForeground(Color.white);
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		mouseOverPanel.add(lblTitle, BorderLayout.NORTH);

		JPanel testPanel = new JPanel(new GridLayout(0, 1));
		testPanel.setOpaque(false);
		lblRating = new JLabel("<html><center>Rating currently unavailable.</center></html>", SwingConstants.CENTER);
		lblRating.setForeground(Color.yellow);
		lblRating.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblRating.setAlignmentX(Component.CENTER_ALIGNMENT);
		mouseOverPanel.add(lblRating, BorderLayout.CENTER);

		lblAddShow = new JLabel("<html><center>+ Add to 'My Shows'</center></html>", SwingConstants.CENTER);
		lblAddShow.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblAddShow.setForeground(Color.white);
		lblAddShow.setFont(new Font("Tahoma", Font.BOLD, 15));

		/* Search to determine if already added to show *yawns* */
		boolean alreadyAdded = false;
		ArrayList<MyShowComponent> allShowComponents = shows.getAllShowComponents();
		Scanner urlScan = new Scanner(url);
		String prev = null;
		String urlParam = null;
		urlScan.useDelimiter("/");
		while (urlScan.hasNext()) {
			String txt = urlScan.next();
			if (txt != null && txt.length() != 0) {
				if (txt.charAt(0) == 't' && txt.charAt(1) == 't' && prev.equals("title")) {
					urlParam = txt;
					break;
				}
			}
			prev = txt;
		}
		for (MyShowComponent c : allShowComponents) {
			if (c.getUrl().equals(url)) {
				alreadyAdded = true;
			} else {
				urlScan = new Scanner(c.getUrl());
				urlScan.useDelimiter("/");
				while (urlScan.hasNext()) {
					String txt = urlScan.next();
					if (txt != null && txt.length() != 0) {
						if (txt.charAt(0) == 't' && txt.charAt(1) == 't' && prev.equals("title")) {
							if (txt.equals(urlParam)) {
								alreadyAdded = true;
								break;
							}
						}
					}
					prev = txt;
				}
			}
			if (alreadyAdded == true) {
				break;
			}
		}
		urlScan.close();

		if (!alreadyAdded) {
			// Make sure no listeners before adding new one
			if (lblAddShow.getMouseListeners() != null) {
				for (MouseListener listener : lblAddShow.getMouseListeners()) {
					lblAddShow.removeMouseListener(listener);
				}
			}

			lblAddShow.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {
					JLabel source = (JLabel) e.getSource();
					source.setForeground(source.getForeground() == Color.white ? Color.white : Color.yellow);
				}

				@Override
				public void mousePressed(MouseEvent e) {
					JLabel source = (JLabel) e.getSource();
					source.setForeground(Color.red);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					JLabel source = (JLabel) e.getSource();
					source.setForeground(Color.white);
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					JLabel source = (JLabel) e.getSource();
					source.setForeground(Color.yellow);
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					JLabel source = (JLabel) e.getSource();
					source.setForeground(Color.red);
					source.setText("<html><center>Adding...</center></html>");
					try {
						MyShowComponent comp = new MyShowComponent(lblTitle.getText(), episodeGuideURL, url,
								lblPosterImage.getIcon(), shows, lblRating.getText());
						comp.fetchEpisodeData();
						shows.addNewShowComponent(comp);
					} catch (Exception ex) {
						System.out.println("Error adding " + lblTitle.getText() + " to my shows.");
						ex.printStackTrace();
						source.setForeground(Color.yellow);
						source.setText("<html><center>✓ Added to 'My Shows'</center></html>");
					}
					source.setForeground(Color.yellow);
					source.setText("<html><center>✓ Added to 'My Shows'</center></html>");
					source.removeMouseListener(this);
				}
			});
		} else {
			lblAddShow.setText("<html><center>✓ Show already in 'My Shows'</center></html>");
		}

		mouseOverPanel.add(lblAddShow, BorderLayout.SOUTH);

		add(mouseOverPanel, new Integer(3));
		fetchAndSetDocumentData();
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}

	public void fetchAndSetDocumentData() {
		Document doc = null;
		try {
			doc = Jsoup.connect(url).timeout(30000).get();
		} catch (SocketTimeoutException e) {
			System.err.println("Socket timed out for URL: " + url);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Document fetch error for URL: " + url);
			e.printStackTrace();
		}
		// Get title
		title = doc.title().replace(" - IMDb", "");
		title = title.substring(0, title.indexOf('(') - 1) + "<br>"
				+ title.substring(title.indexOf('('), title.length());
		lblTitle.setText("<html><center>" + title + "</center></html>");

		/* Gets poster image */
		BufferedImage img = null;
		if (doc != null) {
			try {
				String posterImageSrc = doc.getElementsByClass("poster").first().getElementsByTag("img").first()
						.absUrl("src");
				String posterImageHigherQuality = posterImageSrc.substring(0, posterImageSrc.indexOf('@') + 1);
				try {
					if (posterImageHigherQuality != null) {
						URL imgUrl = new URL(posterImageHigherQuality);
						img = ImageIO.read(imgUrl);
						img = resizeBufferedImage(img);
					}
				} catch (Exception e) {
					try {
						if (posterImageSrc != null) {
							URL imgUrl = new URL(posterImageSrc);
							img = ImageIO.read(imgUrl);
							img = resizeBufferedImage(img);
						}
					} catch (Exception ex) {
						System.err.println("No source image.");
						img = Frame1.getDefaultShowIcon();
						ex.printStackTrace();
					}
				}
			} catch (Exception ex) {
				System.err.println("No source image.");
				try {
					img = Frame1.getDefaultShowIcon();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// ex.printStackTrace();
			}
		}
		lblPosterImage.setIcon(new ImageIcon(img));

		/* Gets show rating */
		String contentRating = doc.select(".ratingValue strong").attr("title");
		contentRating = contentRating.replaceAll(",", "");
		contentRating = contentRating.replaceAll("[^0-9.]", " ");
		contentRating = contentRating.trim();
		Scanner sc = new Scanner(contentRating);
		double rating = 0;
		int users = 0;
		while (sc.hasNext()) {
			try {
				rating = Double.parseDouble(sc.next());
			} catch (Exception e) {
				System.out.println("Double parse error.");
				rating = 0;
			}
			try {
				users = Integer.parseInt(sc.next());
			} catch (Exception e) {
				System.out.println("Int parse error");
				users = 0;
			}
		}
		sc.close();

		if (users > 0 && rating > 0) {
			lblRating.setText("<html><center>" + Double.toString(rating) + "/10" + "<br>"
					+ NumberFormat.getIntegerInstance().format(users) + " votes</center></html>");
		} else {
			lblRating.setText("Rating unavailable.");
			/* Finished show rating */
		}

		/* Get season date */
		episodeGuideURL = doc.select("div.button_panel.navigation_panel").first().getElementsByTag("a")
				.attr("abs:href");
		img.flush();
		img = null;
		doc = null;
		mouseOverPanel.repaint();
		mouseOverPanel.revalidate();
	}

	public BufferedImage resizeBufferedImage(BufferedImage img) {
		javaxt.io.Image tmpImg = new javaxt.io.Image(img);
		tmpImg.resize(MyShowComponent.getPanelWidth(), MyShowComponent.getPanelHeight(), false);
		return tmpImg.getBufferedImage();
	}

	public boolean isMouseOverPanelVisible() {
		return mouseOverPanel.isVisible();
	}

	public void startMaskFadeInAnimation() {
		mask.playFadeInAnimation();
	}

	public void startMaskFadeOutAnimation() {
		mask.playFadeOutAnimation();
	}

	public static int getPanelWidth() {
		return PANEL_WIDTH;
	}

	public static int getPanelHeight() {
		return PANEL_HEIGHT;
	}

	public static int getPanelBorder() {
		return PANEL_BORDER;
	}

	public static int getHoverOverDelay() {
		return HOVER_OVER_DELAY;
	}
}