
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@SuppressWarnings("serial")
public class SearchPanel extends JPanel {

	private final int MAX_ROW_AMOUNT = 5;
	private GridBagConstraints gridC = new GridBagConstraints();
	private final Insets GRID_INSETS = new Insets(5, 5, 5, 5);
	private final static EmptyBorder PANEL_BORDER = new EmptyBorder(5, 10, 5, 10);
	private MyShowsPanel shows;
	private ArrayList<SearchResultComponent> allComps;

	public SearchPanel(MyShowsPanel showsPanel) {
		shows = showsPanel;
		setLayout(new GridBagLayout());
		setBackground(Color.BLACK);
		setBorder(PANEL_BORDER);
		gridC.insets = GRID_INSETS;
	}

	public void imdbSearch(String url, String params) {
		allComps = new ArrayList<>();
		removeAll();
		class NewTask extends SwingWorker<Void, Void> {
			private CharSequence cs = "TV Series";
			private CharSequence cS = "Episode";

			@Override
			protected Void doInBackground() throws Exception {
				ExecutorService executor = Executors.newFixedThreadPool(5);
				class NewThread implements Runnable {
					private Element e;

					public NewThread(Element e) {
						this.e = e;
					}

					@Override
					public void run() {
						if (e.toString().contains(cs) && !e.toString().contains(cS)) {
							String showUrl = e.getElementsByTag("a").attr("abs:href");
							allComps.add(new SearchResultComponent(showUrl, shows));
						}
					}
				}
				Document doc = null;
				ArrayList<Future<?>> taskList = null;
				try {
					doc = Jsoup.connect(url).timeout(5000).get();
					Elements content = doc.getElementsByClass("result_text");
					taskList = new ArrayList<>();
					for (Element e : content) {
						taskList.add(executor.submit(new NewThread(e)));
					}
				} catch (SocketTimeoutException e) {
					JOptionPane.showMessageDialog(SearchPanel.this, "Search timed out. Please try again.");
					executor.shutdown();
					this.cancel(true);
				} catch (Exception e) {
					System.out.println("IMDB search error.");
					executor.shutdown();
					this.cancel(true);
					e.printStackTrace();
				}

				executor.shutdown();
				executor.awaitTermination(20000, TimeUnit.MILLISECONDS);
				int posX = 0;
				int posY = 0;
				if (allComps.size() == 0) {
					gridC.gridx = 0;
					gridC.gridy = -1;
					JLabel emptyLbl = new JLabel("No Results Found For: " + params);
					emptyLbl.setForeground(Color.white);
					emptyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 30));
					add(emptyLbl, gridC);
				} else {
					for (SearchResultComponent searchResultComponent : allComps) {
						if (posX >= MAX_ROW_AMOUNT) {
							posX = 0;
						}
						gridC.gridx = posX;
						gridC.gridy = posY / MAX_ROW_AMOUNT;
						add(searchResultComponent, gridC);
						posX++;
						posY++;
					}
					allComps = null;
					taskList = new ArrayList<>();
				}
				return null;
			}
		}
		NewTask task = new NewTask();
		removeAll();

		gridC.gridx = 0;
		gridC.gridy = -1;
		JLabel dots = new JLabel(".    .    .");
		dots.setForeground(Color.white);
		dots.setFont(new Font("Segoe UI", Font.BOLD, 30));
		Timer t = new Timer(500, e -> {
			if (task.isDone() || task.isCancelled()) {
				((Timer) e.getSource()).stop();
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
		add(dots, gridC);
		t.start();
		task.execute();
	}

	public Insets getComponentInsets() {
		return GRID_INSETS;
	}

	public ArrayList<SearchResultComponent> getAllSearchResults() {
		ArrayList<SearchResultComponent> array = new ArrayList<SearchResultComponent>();
		for (Component c : getComponents()) {
			if (c instanceof SearchResultComponent) {
				array.add((SearchResultComponent) c);
			} else {
				System.err.println("Why tf is there a component other than searchresult in this panel?");
			}
		}

		return array;
	}

	public int getMaxRowAmount() {
		return MAX_ROW_AMOUNT;
	}

	public Insets getGridBagInsets() {
		return GRID_INSETS;
	}
}
