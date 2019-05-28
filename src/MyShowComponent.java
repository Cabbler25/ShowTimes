import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@SuppressWarnings("serial")
public class MyShowComponent extends JLayeredPane {
	private final static int PANEL_WIDTH = 200;
	private final static int PANEL_HEIGHT = 300;
	private final static int PANEL_BORDER = 10;
	private final static int HOVER_OVER_DELAY = 300;
	private Timer hoverDelayTimer, checkMouseTimer;

	private JLabel lblPosterImage = new JLabel();
	private JLabel lblTitle, lblRating, lblEpisodeInfo;
	private String episodeGuideUrl, url, title, ratingText, currentSeason, currentSeasonEpisodeNumber,
			currentSeasonDate, nextSeason, nextSeasonEpisodeNumber, nextSeasonDate;
	private Icon icon;

	private TransparentMask mask;

	private MyShowsPanel shows;
	private JPanel mouseOverPanel;
	private boolean currentlyAiring = false;
	private boolean midSeasonHiatus = false;

	public MyShowComponent(String titleIn, String episodeUrlIn, String showUrlIn, Icon bIconIn,
			MyShowsPanel showPanelIn, String ratingIn) {
		currentSeason = currentSeasonEpisodeNumber = currentSeasonDate = nextSeason = nextSeasonEpisodeNumber = nextSeasonDate = "U/A";
		title = titleIn == null ? "Title unavailable" : titleIn;
		ratingText = ratingIn == null ? "Rating unavailable." : ratingIn;
		episodeGuideUrl = episodeUrlIn == null ? "" : episodeUrlIn;
		url = showUrlIn == null ? "" : showUrlIn;
		shows = showPanelIn;
		icon = bIconIn;
		initialize();
	}

	public MyShowComponent(String titleIn, String episodeUrlIn, String showUrlIn, Icon bIconIn,
			MyShowsPanel showPanelIn, String ratingIn, String curEp, String curEpD, String nextEp, String nextEpD,
			boolean currentlyAiring, boolean midSeasonHiatus) {
		currentSeason = nextSeason = "U/A";
		currentSeasonEpisodeNumber = curEp;
		currentSeasonDate = curEpD;
		nextSeasonEpisodeNumber = nextEp;
		nextSeasonDate = nextEpD;
		title = titleIn == null ? "Title unavailable" : titleIn;
		ratingText = ratingIn == null ? "Rating unavailable." : ratingIn;
		episodeGuideUrl = episodeUrlIn == null ? "" : episodeUrlIn;
		url = showUrlIn == null ? "" : showUrlIn;
		shows = showPanelIn;
		icon = bIconIn;
		this.currentlyAiring = currentlyAiring;
		this.midSeasonHiatus = midSeasonHiatus;
		initialize();
	}

	private void initialize() {
		setOpaque(true);
		setPanelColor();
		setPreferredSize(new Dimension(PANEL_WIDTH + PANEL_BORDER, PANEL_HEIGHT + PANEL_BORDER));
		hoverDelayTimer = new Timer(HOVER_OVER_DELAY, e -> {
			Point currentMousePos = MouseInfo.getPointerInfo().getLocation();
			Point compPoint = isShowing() ? getLocationOnScreen() : new Point(0, 0);
			Rectangle rec = new Rectangle((int) compPoint.getX(), (int) compPoint.getY(), getWidth(), getHeight());
			if (rec.contains(currentMousePos) && isShowing()) {
				startMaskFadeInAnimation();
				mouseOverPanel.setVisible(true);
				checkMouseTimer.restart();
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

		lblPosterImage = new JLabel();
		lblPosterImage.setIcon(icon);
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
		mouseOverPanel.setVisible(false);
		mouseOverPanel.setOpaque(false);
		mouseOverPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		mouseOverPanel.setBounds(PANEL_BORDER / 2, PANEL_BORDER / 2, PANEL_WIDTH, PANEL_HEIGHT);

		lblTitle = new JLabel(title, SwingConstants.CENTER);
		lblTitle.setForeground(Color.white);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
		mouseOverPanel.add(lblTitle, BorderLayout.NORTH);

		GridBagConstraints gdc = new GridBagConstraints();
		JPanel pnlCenter = new JPanel(new GridBagLayout());
		pnlCenter.setOpaque(false);

		lblRating = new JLabel(ratingText, SwingConstants.CENTER);
		lblRating.setForeground(Color.yellow);
		lblRating.setFont(new Font("Segoe UI", Font.BOLD, 15));
		pnlCenter.add(lblRating, gdc);

		lblEpisodeInfo = new JLabel(getFormattedEpisodeString(), SwingConstants.CENTER);
		lblEpisodeInfo.setForeground(Color.white);
		lblEpisodeInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		gdc.gridy = 1;
		gdc.insets = new Insets(20, 0, 0, 0);
		pnlCenter.add(lblEpisodeInfo, gdc);

		mouseOverPanel.add(pnlCenter, BorderLayout.CENTER);

		gdc = new GridBagConstraints();
		JPanel pnlSouth = new JPanel(new GridBagLayout());
		pnlSouth.setOpaque(false);

		JLabel lblRefresh = new JLabel("<html><font color=yellow>↻</font> Refresh</html>", SwingConstants.CENTER);
		lblRefresh.setForeground(Color.white);
		lblRefresh.setFont(new Font("DejaVu Sans", Font.PLAIN, 13));
		lblRefresh.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				((JLabel) e.getSource()).setForeground(
						((JLabel) e.getSource()).getForeground() == Color.white ? Color.white : Color.yellow);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				((JLabel) e.getSource()).setForeground(Color.red);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				((JLabel) e.getSource()).setForeground(Color.white);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				((JLabel) e.getSource()).setForeground(Color.yellow);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				class RefreshTask extends SwingWorker<Void, Void> {

					@Override
					protected Void doInBackground() throws Exception {
						shows.updateSingleShow(MyShowComponent.this, true);
						return null;
					}
				}
				removeAll();
				RefreshTask task = new RefreshTask();
				JLabel dots = new JLabel(".    .    .");
				dots.setForeground(Color.white);
				dots.setFont(new Font("Segoe UI", Font.BOLD, 30));

				Timer t = new Timer(500, e2 -> {
					if (task.isDone() || task.isCancelled()) {
						((Timer) e2.getSource()).stop();
						remove(dots);
						repaint();
						revalidate();
					} else {
						String txt = dots.getText();
						if (txt.equals(".")) {
							dots.setText(".    .");
						} else if (txt.equals(".    .")) {
							dots.setText(".    .    .");
						} else if (txt.equals(".    .    .")) {
							dots.setText(".");
						}

						repaint();
						revalidate();
					}
				});
				JPanel tmp = new JPanel();
				tmp.setBackground(Color.BLACK);
				tmp.setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = -1;
				tmp.add(dots, gbc);
				tmp.setBounds(PANEL_BORDER / 2, PANEL_BORDER / 2, PANEL_WIDTH, PANEL_HEIGHT);
				add(tmp, new Integer(0));
				t.start();
				task.execute();
			}
		});
		pnlSouth.add(lblRefresh, gdc);

		JLabel lblDelete = new JLabel("<html><center><font color=red>×</font> Delete</center></html>",
				SwingConstants.CENTER);
		lblDelete.setForeground(Color.white);
		lblDelete.setFont(new Font("DejaVu Sans", Font.PLAIN, 13));
		lblDelete.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				JLabel source = (JLabel) e.getSource();
				source.setForeground(source.getForeground() == Color.white ? Color.white : Color.red);
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
				source.setForeground(Color.red);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				int option = JOptionPane.showConfirmDialog(MyShowComponent.this,
						"Are you sure you wish to delete this show?", "Confirm delete", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					shows.deleteShowComponent(MyShowComponent.this);
				}
			}
		});
		gdc.gridy = 1;
		pnlSouth.add(lblDelete, gdc);

		mouseOverPanel.add(pnlSouth, BorderLayout.SOUTH);
		add(mouseOverPanel, new Integer(3));
	}

	public void fetchEpisodeData() {
		Document doc = null;
		try {
			doc = Jsoup.connect(episodeGuideUrl).timeout(5000).get();
		} catch (SocketTimeoutException e) {
			System.err.println("Timed out: " + url);
			JOptionPane.showMessageDialog(this.getParent(), "Socket timed out for " + url + System.lineSeparator()
					+ " while fetching episode data. Please try again.");
		} catch (IOException e) {
			System.out.println("Fetch episode data error.");
			e.printStackTrace();
		}

		// Get first season info 
		currentSeason = doc.select("#bySeason > option[selected]").text();
		if (currentSeason.equalsIgnoreCase("unknown")) {
			// Just gonna assume first season is current season
			currentSeason = doc.select("#bySeason > option:nth-child(1)").text();
			// Gotta reconnect too ;(
			try {
				doc = Jsoup.connect(episodeGuideUrl.substring(0, episodeGuideUrl.indexOf('?') + 1) + "season=" + 1)
						.timeout(5000).get();
			} catch (SocketTimeoutException e) {
				System.err.println("Timed out: " + url);
			} catch (IOException e) {
				System.out.println("Fetch episode data error.");
				e.printStackTrace();
			}
		}
		currentSeasonEpisodeNumber = doc.select(
				"#episodes_content > div.clear > div.list.detail.eplist > div:last-of-type > div.image > div > div")
				.text();
		if (currentSeasonEpisodeNumber == null || currentSeasonEpisodeNumber.replaceAll("\\s+", "").length() == 0) {
			currentSeasonEpisodeNumber = doc.select(
					"#episodes_content > div.clear > div.list.detail.eplist > div:last-of-type > div.image > a > div > div")
					.text();
		}
		currentSeasonDate = doc.select(
				"#episodes_content > div.clear > div.list.detail.eplist > div:last-of-type > div.info > div.airdate")
				.text();

		String pattern = "dd MMM yyyy";
		String date = currentSeasonDate.replaceAll("\\.", "");
		Date currentEpisodeDate = null;
		Date currentDate = new Date();
		boolean dateThrowsException;

		try {
			currentEpisodeDate = new SimpleDateFormat(pattern).parse(date);
			dateThrowsException = false;
		} catch (Exception e) {
			dateThrowsException = true;
		}

		int childIndex = Integer.parseInt(doc.select("#episodes_content > div.clear > meta").first().attr("content"));
		if (dateThrowsException) {
			for (int i = childIndex - 1; i > 0; i--) {
				String dir = "#episodes_content > div.clear > div.list.detail.eplist > div:nth-child(" + i
						+ ") > div.info > div.airdate";
				childIndex = Integer
						.parseInt(doc.select("#episodes_content > div.clear > div.list.detail.eplist > div:nth-child("
								+ i + ") > div.info > meta").first().attr("content"));
				String eDir = "#episodes_content > div.clear > div.list.detail.eplist > div:nth-child(" + i
						+ ") > div.image > a > div > div";
				currentSeasonDate = doc.select(dir).text();
				currentSeasonEpisodeNumber = doc.select(eDir).text();
				if (currentSeasonEpisodeNumber == null
						|| currentSeasonEpisodeNumber.replaceAll("\\s+", "").length() == 0) {
					currentSeasonEpisodeNumber = doc
							.select("#episodes_content > div.clear > div.list.detail.eplist > div:nth-child(" + i
									+ ") > div.image > div > div")
							.text();
				}
				try {
					currentEpisodeDate = new SimpleDateFormat(pattern).parse(currentSeasonDate.replaceAll("\\.", ""));
					break;
				} catch (Exception e) {
					if (i == 1) {
						System.out.println("No parceable date found for current season.");
						currentEpisodeDate = null;
						break;
					}
				}
			}
		}
		if (currentEpisodeDate != null) {
			Calendar cal1 = Calendar.getInstance();
			Calendar cal2 = Calendar.getInstance();
			cal1.setTime(currentEpisodeDate);
			cal2.setTime(currentDate);

			if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
					&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
				currentlyAiring = true;
				setBackground(Color.red);
			} else if (!currentEpisodeDate.before(currentDate)) {
				currentlyAiring = true;
				Date hold = currentEpisodeDate;
				String holdThisEp = currentSeasonEpisodeNumber;
				String holdThisDate = currentSeasonDate;

				for (int i = childIndex - 1; i > 0; i--) {
					String dir = "#episodes_content > div.clear > div.list.detail.eplist > div:nth-child(" + i
							+ ") > div.info > div.airdate";
					String thisDate = doc.select(dir).text();
					String thisDateEdit = thisDate.replaceAll("\\.", "");
					Date prevEpisodeDate = null;

					try {
						prevEpisodeDate = new SimpleDateFormat(pattern).parse(thisDateEdit);
						if (prevEpisodeDate.before(currentDate)) {
							String eDir = "#episodes_content > div.clear > div.list.detail.eplist > div:nth-child(" + i
									+ ") > div.image > a > div > div";
							currentSeasonEpisodeNumber = doc.select(eDir).text();
							if (currentSeasonEpisodeNumber == null
									|| currentSeasonEpisodeNumber.replaceAll("\\s+", "").length() == 0) {
								currentSeasonEpisodeNumber = doc.select(
										"#episodes_content > div.clear > div.list.detail.eplist > div:nth-child(" + i
												+ ") > div.image > div > div")
										.text();
							}
							currentSeasonDate = thisDate;
							i = 0;
							if ((hold.getTime() - prevEpisodeDate.getTime()) > 2592000000L) {
								currentlyAiring = false;
								midSeasonHiatus = true;
								setPanelColor();
								nextSeasonEpisodeNumber = holdThisEp;
								nextSeason = currentSeason;
								nextSeasonDate = holdThisDate;
								lblEpisodeInfo.setText(getFormattedEpisodeString());
								return;
							} else {
								midSeasonHiatus = false;
							}
						}
						String eDirPrev = "#episodes_content > div.clear > div.list.detail.eplist > div:nth-child(" + i
								+ ") > div.image > a > div > div";
						hold = prevEpisodeDate;
						holdThisDate = thisDate;
						holdThisEp = doc.select(eDirPrev).text();
						if (holdThisEp == null || holdThisEp.length() == 0) {
							holdThisEp = doc
									.select("#episodes_content > div.clear > div.list.detail.eplist > div:nth-child("
											+ i + ") > div.image > div > div")
									.text();
						}
					} catch (Exception e) {
						break;
					}
				}
			} else {
				currentlyAiring = false;
			}

		} else {
			currentlyAiring = false;
		}
		setPanelColor();

		/* Get next season info */
		String temp = "#bySeason > option:nth-child(" + (Integer.parseInt(currentSeason) + 1) + ")";
		nextSeason = doc.select(temp).text();
		int next = 0;
		int prev = 1;
		try {
			next = Integer.parseInt(nextSeason);
			prev = Integer.parseInt(currentSeason);
		} catch (Exception e) {
			System.out.println("Next season parse int error for " + title);
		}
		if (!currentSeason.equals(nextSeason) && next > prev) { // If current season is not final season
			String tempURL = episodeGuideUrl.substring(0, episodeGuideUrl.indexOf('?') + 1) + "season=" + nextSeason;

			try {
				Document nextSeasonDoc = Jsoup.connect(tempURL).get();
				nextSeasonEpisodeNumber = nextSeasonDoc.select(
						"#episodes_content > div.clear > div.list.detail.eplist > div:first-of-type > div.image > a > div > div")
						.text();
				if (nextSeasonEpisodeNumber == null || nextSeasonEpisodeNumber.length() == 0) {
					nextSeasonEpisodeNumber = nextSeasonDoc.select(
							"#episodes_content > div.clear > div.list.detail.eplist > div:first-of-type > div.image > div > div")
							.text();
				}
				nextSeasonDate = nextSeasonDoc.select(
						"#episodes_content > div.clear > div.list.detail.eplist > div:first-of-type > div.info > div.airdate")
						.text();
			} catch (Exception e) {
				System.out.println("Fetch next season document error for " + title);
				nextSeasonEpisodeNumber = "U/A";
				nextSeasonDate = "U/A";
			}
		} else {
			nextSeason = nextSeasonDate = nextSeasonEpisodeNumber = "U/A";
		}
		lblEpisodeInfo.setText(getFormattedEpisodeString());
	}

	public String getFormattedEpisodeString() {
		String top;
		String bottom;
		if ((currentSeason.equals("U/A") || currentSeason.equals("") || currentSeason == null)
				&& (currentSeasonDate.equals("U/A") || currentSeasonDate.equals("") || currentSeasonDate == null)
				&& (currentSeasonEpisodeNumber.equals("U/A") || currentSeasonEpisodeNumber.equals("")
						|| currentSeasonEpisodeNumber == null)) {
			top = "<html><center>Current season unavailable.<br>";
		} else {
			top = "<html><center>" + "<b>Current:</b> " + currentSeasonEpisodeNumber + "<br>" + "Aired on "
					+ currentSeasonDate + (midSeasonHiatus ? "<br><b>(Mid-season hiatus)</b><br>" : "<br>");
		}

		if ((nextSeason.equals("U/A") || nextSeason.equals("") || nextSeason == null)
				&& (nextSeasonDate.equals("U/A") || nextSeasonDate.equals("") || nextSeasonDate == null)
				&& (nextSeasonEpisodeNumber.equals("U/A") || nextSeasonEpisodeNumber.equals("")
						|| nextSeasonEpisodeNumber == null)) {
			bottom = "<br>Next season unavailable.</center></html>";
		} else {
			if (nextSeasonDate == null || nextSeasonDate.length() <= 0) {
				bottom = "<b>Next:</b> " + nextSeasonEpisodeNumber + "<br>Date unavailable.";
			} else {
				bottom = "<b>Next:</b> " + nextSeasonEpisodeNumber + "<br>Airing"
						+ (nextSeasonDate.matches("[0-9]+") || Character.isLetter(nextSeasonDate.charAt(0)) ? " in "
								: " on ")
						+ nextSeasonDate + "</center></html>";
			}
		}

		return top + bottom;
	}

	private void setPanelColor() {
		if (currentlyAiring) {
			String pattern = "dd MMM yyyy";
			String date = currentSeasonDate.replaceAll("\\.", "");
			Date currentEpisodeDate;
			try {
				currentEpisodeDate = new SimpleDateFormat(pattern).parse(date);
			} catch (ParseException e) {
				currentEpisodeDate = null;
			}
			Date currentDate = new Date();
			Calendar cal1 = Calendar.getInstance();
			Calendar cal2 = Calendar.getInstance();
			cal1.setTime(currentEpisodeDate);
			cal2.setTime(currentDate);
			if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
					&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
				setBackground(Color.red);
			} else {
				setBackground(Color.yellow);
			}
		} else {
			setBackground(new Color(66, 66, 66));
		}
	}

	// Returns image for saving
	public Image getButtonImage() {
		Icon icon = lblPosterImage.getIcon();
		int w = icon.getIconWidth();
		int h = icon.getIconHeight();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);
		Graphics2D g = image.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		return image;
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

	public Icon getButtonIcon() {
		return lblPosterImage.getIcon();
	}

	public void setlblPosterImage(JLabel lblPosterImage) {
		this.lblPosterImage = lblPosterImage;
	}

	public String getEpisodeGuideUrl() {
		return episodeGuideUrl;
	}

	public void setEpisodeGuideUrl(String episodeGuideUrl) {
		this.episodeGuideUrl = episodeGuideUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRatingText() {
		return ratingText;
	}

	public void setRatingText(String ratingText) {
		this.ratingText = ratingText;
	}

	public String getCurrentSeason() {
		return currentSeason;
	}

	public void setCurrentSeason(String currentSeason) {
		this.currentSeason = currentSeason;
	}

	public String getCurrentSeasonEpisodeNumber() {
		return currentSeasonEpisodeNumber;
	}

	public void setCurrentSeasonEpisodeNumber(String currentSeasonEpisodeNumber) {
		this.currentSeasonEpisodeNumber = currentSeasonEpisodeNumber;
	}

	public String getCurrentSeasonDate() {
		return currentSeasonDate;
	}

	public void setCurrentSeasonDate(String currentSeasonDate) {
		this.currentSeasonDate = currentSeasonDate;
	}

	public String getNextSeason() {
		return nextSeason;
	}

	public void setNextSeason(String nextSeason) {
		this.nextSeason = nextSeason;
	}

	public String getNextSeasonEpisodeNumber() {
		return nextSeasonEpisodeNumber;
	}

	public void setNextSeasonEpisodeNumber(String nextSeasonEpisodeNumber) {
		this.nextSeasonEpisodeNumber = nextSeasonEpisodeNumber;
	}

	public String getNextSeasonDate() {
		return nextSeasonDate;
	}

	public void setNextSeasonDate(String nextSeasonDate) {
		this.nextSeasonDate = nextSeasonDate;
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

	public boolean isCurrentlyAiring() {
		return currentlyAiring;
	}

	public void setCurrentlyAiring(boolean b) {
		currentlyAiring = b;
		setPanelColor();
	}

	public boolean isMidSeasonHiatus() {
		return midSeasonHiatus;
	}

	public void setMidSeasonHiatus(boolean midSeasonHiatus) {
		this.midSeasonHiatus = midSeasonHiatus;
	}
}
