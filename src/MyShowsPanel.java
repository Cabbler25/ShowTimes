import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@SuppressWarnings("serial")
public class MyShowsPanel extends JPanel {
	private final static int MAX_ROW_AMOUNT = 5;
	private GridBagConstraints gridC = new GridBagConstraints();
	private final static Insets GRID_INSETS = new Insets(5, 5, 5, 5);
	private final static EmptyBorder PANEL_BORDER = new EmptyBorder(5, 10, 5, 10);
	private ArrayList<MyShowComponent> allShowComponents;

	public MyShowsPanel() {
		allShowComponents = new ArrayList<MyShowComponent>();
		setBackground(Color.BLACK);
		setLayout(new GridBagLayout());
		setBorder(PANEL_BORDER);
		gridC.insets = GRID_INSETS;
		loadShowComponents();
	}

	public void addNewShowComponent(MyShowComponent show) {
		allShowComponents.add(show);
		sortShowsByDate();
		addShowComponentsToPanel();
	}

	private void addShowComponentsToPanel() {
		removeAll();
		int posX = 0;
		int posY = 0;
		gridC.insets = GRID_INSETS;
		for (MyShowComponent c : allShowComponents) {
			if (posX >= MAX_ROW_AMOUNT) {
				posX = 0;
			}
			gridC.gridx = posX;
			gridC.gridy = posY / MAX_ROW_AMOUNT;
			add(c, gridC);
			posX++;
			posY++;
		}
		repaint();
		revalidate();
	}

	private void loadShowComponents() {
		Scanner sc = null, lineScan = null;
		File file = new File("./resources/saved variables/savedshows.xml");
		try {
			try {
				sc = new Scanner(file);
			} catch (FileNotFoundException e) {
				file.createNewFile();
				sc = new Scanner(file);
			}
			int index;
			Icon icon = null;
			String title = null;
			String episode = null;
			String url = null;
			String rating = null;
			String currentSeasonEpisode = null;
			String currentSeasonEpisodeDate = null;
			String nextSeasonEpisode = null;
			String nextSeasonEpisodeDate = null;
			boolean currentlyAiring = false;
			boolean midSeasonHiatus = false;
			while (sc.hasNext()) {
				String line = sc.nextLine();
				lineScan = new Scanner(line);
				lineScan.useDelimiter("\"");
				if (line.length() != 0) {
					if (line.charAt(0) == '<') {
						if (lineScan.hasNext()) {
							String type = lineScan.next().replaceAll("[^A-Za-z]+", "");
							if (type.equals("index")) {
								index = lineScan.nextInt();
								icon = new ImageIcon(loadImage(index));
							} else if (type.equalsIgnoreCase("title")) {
								title = lineScan.next();
							} else if (type.equalsIgnoreCase("episodeUrl")) {
								episode = lineScan.next();
							} else if (type.equalsIgnoreCase("showURL")) {
								url = lineScan.next();
							} else if (type.equalsIgnoreCase("showRating")) {
								rating = lineScan.next();
							} else if (type.equalsIgnoreCase("isCurrentlyAiring")) {
								if (lineScan.next().equalsIgnoreCase("true")) {
									currentlyAiring = true;
								} else {
									currentlyAiring = false;
								}
							} else if (type.equalsIgnoreCase("inMidSeasonHiatus")) {
								if (lineScan.next().equalsIgnoreCase("true")) {
									midSeasonHiatus = true;
								} else {
									midSeasonHiatus = false;
								}
							} else if (type.equalsIgnoreCase("currentSeasonEpisode")) {
								currentSeasonEpisode = lineScan.next();
							} else if (type.equalsIgnoreCase("currentSeasonDate")) {
								currentSeasonEpisodeDate = lineScan.next();
							} else if (type.equalsIgnoreCase("nextSeasonEpisode")) {
								nextSeasonEpisode = lineScan.next();
							} else if (type.equalsIgnoreCase("nextSeasonDate")) {
								nextSeasonEpisodeDate = lineScan.next();
								MyShowComponent comp = new MyShowComponent(title, episode, url, icon, this, rating,
										currentSeasonEpisode, currentSeasonEpisodeDate, nextSeasonEpisode,
										nextSeasonEpisodeDate, currentlyAiring, midSeasonHiatus);
								allShowComponents.add(comp);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
			if (lineScan != null) {
				lineScan.close();
			}
		}
		sortShowsByDate();
		addShowComponentsToPanel();
	}

	private BufferedImage loadImage(int i) throws IOException {
		try {
			return ImageIO.read(new File("./resources/saved variables/images/" + i + ".png"));
		} catch (Exception e) {
			System.err.println("Image loading error.");
			return Frame1.getDefaultShowIcon();
		}
	}

	public int getMaxRowAmount() {
		return MAX_ROW_AMOUNT;
	}

	public Insets getGridBagInsets() {
		return GRID_INSETS;
	}

	public void saveShowComponents() {
		try {
			for (File file : new File("./resources/saved variables/images/").listFiles()) {
				file.delete();
			}

			File propertyFile = new File("./resources/saved variables/savedshows.xml");
			BufferedWriter bw = new BufferedWriter(new FileWriter(propertyFile));
			bw.write("#" + new Date() + System.lineSeparator());
			bw.close();
			bw = new BufferedWriter(new FileWriter(propertyFile, true));

			for (int i = 0; i < allShowComponents.size(); i++) {
				MyShowComponent comp = allShowComponents.get(i);
				bw.write("<index=" + "\"" + Integer.toString(i) + "\">" + System.lineSeparator());
				bw.write("<title=" + "\"" + comp.getTitle() + "\">" + System.lineSeparator());
				bw.write("<episodeUrl=" + "\"" + comp.getEpisodeGuideUrl() + "\">" + System.lineSeparator());
				bw.write("<showURL=" + "\"" + comp.getUrl() + "\">" + System.lineSeparator());
				bw.write("<showRating=" + "\"" + comp.getRatingText() + "\">" + System.lineSeparator());
				bw.write("<isCurrentlyAiring=" + "\"" + comp.isCurrentlyAiring() + "\">" + System.lineSeparator());
				bw.write("<inMidSeasonHiatus=" + "\"" + comp.isMidSeasonHiatus() + "\">" + System.lineSeparator());
				bw.write("<currentSeasonEpisode=" + "\"" + comp.getCurrentSeasonEpisodeNumber() + "\">"
						+ System.lineSeparator());
				bw.write("<currentSeasonDate=" + "\"" + comp.getCurrentSeasonDate() + "\">" + System.lineSeparator());
				bw.write("<nextSeasonEpisode=" + "\"" + comp.getNextSeasonEpisodeNumber() + "\">"
						+ System.lineSeparator());
				bw.write("<nextSeasonDate=" + "\"" + comp.getNextSeasonDate() + "\">" + System.lineSeparator()
						+ System.lineSeparator());

				Image image = comp.getButtonImage();
				BufferedImage img = new BufferedImage(image.getWidth(null), image.getHeight(null),
						BufferedImage.TYPE_INT_RGB);
				Graphics2D bGr = img.createGraphics();
				bGr.drawImage(image, 0, 0, null);
				bGr.dispose();

				ImageIO.write(img, "png", new File("./resources/saved variables/images/" + i + ".png"));
				img.flush();
			}
			bw.close();
		} catch (Exception e) {
			System.err.println("Save unsuccessful");
			e.printStackTrace();
		}
	}

	public void deleteShowComponent(MyShowComponent c) {
		for (MyShowComponent thisComp : allShowComponents) {
			if (thisComp == c) {
				allShowComponents.remove(thisComp);
				addShowComponentsToPanel();
				return;
			}
		}
	}

	public void sortShowsByDate() {
		if (allShowComponents.size() != 0) {
			String pattern = "dd MMM yyyy";
			for (int i = 0; i < allShowComponents.size(); i++) {
				MyShowComponent cI = allShowComponents.get(i);
				String stringNextDate = cI.getNextSeasonDate();
				stringNextDate = stringNextDate.replaceAll("\\.", "");
				String stringCurrentDate = cI.getCurrentSeasonDate();
				stringCurrentDate = stringCurrentDate.replaceAll("\\.", "");
				Date iNextDate;
				Date iCurrentDate;
				try {
					iCurrentDate = new SimpleDateFormat(pattern).parse(stringCurrentDate);
				} catch (ParseException e) {
					try {
						iCurrentDate = new SimpleDateFormat("MMM yyyy").parse(stringCurrentDate);
					} catch (ParseException e1) {
						try {
							iCurrentDate = new SimpleDateFormat("yyyy").parse(stringCurrentDate);
						} catch (ParseException e2) {
							iCurrentDate = null;
						}
					}
				}
				try {
					iNextDate = new SimpleDateFormat(pattern).parse(stringNextDate);
				} catch (ParseException e) {
					try {
						iNextDate = new SimpleDateFormat("MMM yyyy").parse(stringNextDate);
					} catch (ParseException e1) {
						try {
							stringNextDate = Integer.toString(Integer.parseInt(stringNextDate) + 1);
							iNextDate = new SimpleDateFormat("yyyy").parse(stringNextDate);
						} catch (Exception e2) {
							iNextDate = null;
						}
					}
				}
				for (int j = i + 1; j < allShowComponents.size(); j++) {
					MyShowComponent cJ = allShowComponents.get(j);
					String stringDateTwo = cJ.getNextSeasonDate();
					stringDateTwo = stringDateTwo.replaceAll("\\.", "");
					Date jNextDate;
					try {
						jNextDate = new SimpleDateFormat(pattern).parse(stringDateTwo);
					} catch (ParseException e) {
						try {
							jNextDate = new SimpleDateFormat("MMM yyyy").parse(stringDateTwo);
						} catch (ParseException e1) {
							try {
								stringDateTwo = Integer.toString(Integer.parseInt(stringDateTwo) + 1);
								jNextDate = new SimpleDateFormat("yyyy").parse(stringDateTwo);
							} catch (Exception e2) {
								jNextDate = null;
							}
							jNextDate = null;
						}
					}
					if (cJ.isCurrentlyAiring() && !cI.isCurrentlyAiring()) {
						allShowComponents.set(i, cJ);
						allShowComponents.set(j, cI);
						iNextDate = jNextDate;
						cI = cJ;
					} else if (cJ.isCurrentlyAiring() && cI.isCurrentlyAiring()) {
						String stringDateTwoCurrent = cJ.getCurrentSeasonDate();
						stringDateTwoCurrent = stringDateTwoCurrent.replaceAll("\\.", "");
						Date jCurrentDate;
						try {
							jCurrentDate = new SimpleDateFormat(pattern).parse(stringDateTwoCurrent);
						} catch (ParseException e) {
							try {
								jCurrentDate = new SimpleDateFormat("MMM yyyy").parse(stringDateTwoCurrent);
							} catch (ParseException e1) {
								try {
									jCurrentDate = new SimpleDateFormat("yyyy").parse(stringDateTwoCurrent);
								} catch (ParseException e2) {
									jCurrentDate = null;
								}
							}
						}
						if (jCurrentDate != null) {
							if (iCurrentDate == null || jCurrentDate.after(iCurrentDate)) {
								allShowComponents.set(i, cJ);
								allShowComponents.set(j, cI);
								iNextDate = jNextDate;
								iCurrentDate = jCurrentDate;
								cI = cJ;
							}
						}
					} else if (jNextDate != null && !cI.isCurrentlyAiring()) {
						if (iNextDate == null || jNextDate.before(iNextDate)) {
							allShowComponents.set(i, cJ);
							allShowComponents.set(j, cI);
							iNextDate = jNextDate;
							cI = cJ;
						}
					}
				}
			}
		}
	}

	public void updateAllShows() {
		removeAll();
		class NewTask extends SwingWorker<Void, Void> {

			@Override
			protected Void doInBackground() throws Exception {
				long start = System.currentTimeMillis();
				ExecutorService executor = Executors.newFixedThreadPool(allShowComponents.size());
				class NewThread implements Callable<MyShowComponent> {
					private MyShowComponent c;

					public NewThread(MyShowComponent c) {
						this.c = c;
					}

					@Override
					public MyShowComponent call() {
						return updateShowAsync(c);
					}
				}
				ArrayList<Future<MyShowComponent>> taskList = new ArrayList<>();
				for (MyShowComponent myShowComponent : allShowComponents) {
					taskList.add(executor.submit(new NewThread(myShowComponent)));
				}

				ArrayList<MyShowComponent> temp = new ArrayList<>();
				for (Future<MyShowComponent> future : taskList) {
					try {
						temp.add(future.get());
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				executor.shutdown();

				// Wait max of 20 seconds for shutdown
				executor.awaitTermination(20000, TimeUnit.MILLISECONDS);

				// Set some arrays to null in hopes of memory cleanup
				taskList = null;
				allShowComponents = temp;
				temp = null;
				sortShowsByDate();
				addShowComponentsToPanel();
				System.gc();
				System.out.println(System.currentTimeMillis() - start);
				return null;
			}
		}

		NewTask task = new NewTask();
		gridC.gridx = 0;
		gridC.gridy = 0;
		JLabel dots = new JLabel(".    .    .");
		dots.setForeground(Color.white);
		dots.setFont(new Font("Segoe UI", Font.BOLD, 30));
		Timer t = new Timer(500, e -> {
			if (task.isDone() || task.isCancelled()) {
				if (e.getSource() instanceof Timer) {
					((Timer) e.getSource()).stop();
				} else {
					System.out.println("Weird timer error lol");
				}
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
		add(dots, gridC);
		t.start();
		task.execute();
	}

	public boolean updateSingleShow(MyShowComponent c, boolean shouldRefresh) {
		Document doc = null;
		try {
			doc = Jsoup.connect(c.getUrl()).get();
		} catch (Exception e) {
			System.err.println("Update failed, URL now dead: " + c.getUrl());
			JOptionPane.showMessageDialog(this, "Update failed, URL now dead: " + c.getUrl() + System.lineSeparator()
					+ "Please delete this show or try again.");
			e.printStackTrace();
			return false;
		}

		// Get title
		String title = doc.title().replace(" - IMDb", "");
		title = "<html><center>" + title.substring(0, title.indexOf('(') - 1) + "<br>"
				+ title.substring(title.indexOf('('), title.length()) + "</center></html>";

		// Gets poster image
		BufferedImage img = null;
		if (doc != null) {
			String posterImageSrc = null;
			String posterImageHigherQuality = null;

			try {
				posterImageSrc = doc.getElementsByClass("poster").first().getElementsByTag("img").first().absUrl("src");
				posterImageHigherQuality = posterImageSrc.substring(0, posterImageSrc.indexOf('@') + 1);
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
				ex.printStackTrace();
			}
		}
		// Poster image done

		// Gets show rating
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
				System.err.println("Double parse error.");
				rating = 0;
			}
			try {
				users = Integer.parseInt(sc.next());
			} catch (Exception e) {
				System.err.println("Int parse error");
				users = 0;
			}
		}
		sc.close();

		String newRating;
		if (users > 0 && rating > 0) {
			newRating = "<html><center>" + Double.toString(rating) + "/10" + "<br>"
					+ NumberFormat.getIntegerInstance().format(users) + " votes</center></html>";
		} else {
			newRating = "Rating unavailable.";
			// Finished show rating
		}

		// Get episode
		String episodeGuideURL = doc.select("div.button_panel.navigation_panel").first().getElementsByTag("a")
				.attr("abs:href");
		MyShowComponent newComponent = new MyShowComponent(title, episodeGuideURL, c.getUrl(), new ImageIcon(img), this,
				newRating);
		newComponent.fetchEpisodeData();

		// Clear up some mem
		img.flush();
		img = null;
		doc = null;

		for (int i = 0; i < allShowComponents.size(); i++) {
			if (c == allShowComponents.get(i)) {
				allShowComponents.set(i, newComponent);
				break;
			}
		}

		if (shouldRefresh) {
			sortShowsByDate();
			addShowComponentsToPanel();
		}
		return true;
	}

	public MyShowComponent updateShowAsync(MyShowComponent c) {
		Document doc = null;
		try {
			doc = Jsoup.connect(c.getUrl()).get();
		} catch (SocketTimeoutException e) {
			System.err.println("Timed out: " + c.getUrl());
			JOptionPane.showMessageDialog(this, "URL timeout: " + c.getUrl() + System.lineSeparator()
					+ System.lineSeparator() + "Please try again.");
			return c;

		} catch (Exception e) {
			System.err.println("Update failed for show: " + c.getTitle());
			JOptionPane.showMessageDialog(this, "Update failed for show: " + c.getTitle() + System.lineSeparator()
					+ "Please manually update or delete this show.");
			e.printStackTrace();
			return c;
		}

		// Get title
		String title = doc.title().replace(" - IMDb", "");
		title = "<html><center>" + title.substring(0, title.indexOf('(') - 1) + "<br>"
				+ title.substring(title.indexOf('('), title.length()) + "</center></html>";

		// Gets poster image
		BufferedImage img = null;
		if (doc != null) {
			String posterImageSrc = null;
			String posterImageHigherQuality = null;

			try {
				posterImageSrc = doc.getElementsByClass("poster").first().getElementsByTag("img").first().absUrl("src");
				posterImageHigherQuality = posterImageSrc.substring(0, posterImageSrc.indexOf('@') + 1);
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
				ex.printStackTrace();
			}
		}
		// Poster image done

		// Gets show rating
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
				System.err.println("Double parse error.");
				rating = 0;
			}
			try {
				users = Integer.parseInt(sc.next());
			} catch (Exception e) {
				System.err.println("Int parse error");
				users = 0;
			}
		}
		sc.close();

		String newRating;
		if (users > 0 && rating > 0) {
			newRating = "<html><center>" + Double.toString(rating) + "/10" + "<br>"
					+ NumberFormat.getIntegerInstance().format(users) + " votes</center></html>";
		} else {
			newRating = "Rating unavailable.";
			// Finished show rating
		}

		// Get episode
		String episodeGuideURL = doc.select("div.button_panel.navigation_panel").first().getElementsByTag("a")
				.attr("abs:href");
		MyShowComponent newComponent = new MyShowComponent(title, episodeGuideURL, c.getUrl(), new ImageIcon(img), this,
				newRating);
		newComponent.fetchEpisodeData();
		img.flush();
		img = null;
		return newComponent;
	}

	public BufferedImage resizeBufferedImage(BufferedImage img) {
		javaxt.io.Image tmpImg = new javaxt.io.Image(img);
		tmpImg.resize(MyShowComponent.getPanelWidth(), MyShowComponent.getPanelHeight(), false);
		return tmpImg.getBufferedImage();
	}

	public ArrayList<MyShowComponent> getAllShowComponents() {
		return allShowComponents;
	}

	public static int getOptimalDisplayHeight() {
		return 2 * (MyShowComponent.getPanelBorder() * 2 + MyShowComponent.getPanelHeight() + GRID_INSETS.top
				+ GRID_INSETS.bottom) + PANEL_BORDER.getBorderInsets().top;
	}

	public static int getOptimalDisplayWidth() {
		return MAX_ROW_AMOUNT
				* (MyShowComponent.getPanelBorder() * 2 + MyShowComponent.getPanelWidth() + GRID_INSETS.left
						+ GRID_INSETS.right)
				+ PANEL_BORDER.getBorderInsets().left + PANEL_BORDER.getBorderInsets().right;
	}
}
