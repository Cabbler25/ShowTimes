import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class TransparentMask extends JPanel {

	private final int MAX_FADE_IN_TIME = 300;
	private final int MAX_FADE_OUT_TIME = 300;
	private final int MAX_ALPHA_VALUE = 190;
	private final int MIN_ALPHA_VALUE = 0;
	private final int FADE_IN_TIMER_DELAY = 17; // 60 refreshes/second
	private final int FADE_OUT_TIMER_DELAY = 17; // 60 refreshes/second
	private Color maskColor;
	private long elapsedTime;
	private int red, green, blue;
	private int delayAnimationTime = 500; // only play animation if true after 500 millisecs
	private Timer animationDelayTimer, fadeInTimer, fadeOutTimer;
	private int width, height;
	private boolean isFadingOut = false;
	private boolean isFadingIn = false;
	private boolean playAnimation = false;
	private boolean hasDelay = false;
	private boolean delayTimerStarted = false;

	public TransparentMask(int r, int g, int b, int widthIn, int heightIn, int animationDelayIn) {
		red = r;
		green = g;
		blue = b;
		width = widthIn;
		height = heightIn;
		maskColor = new Color(red, green, blue, MIN_ALPHA_VALUE);
		delayAnimationTime = animationDelayIn;
		setOpaque(false);
		initializeTimers();
		hasDelay = true;
	}

	public TransparentMask(int r, int g, int b, int widthIn, int heightIn) {
		red = r;
		green = g;
		blue = b;
		width = widthIn;
		height = heightIn;
		maskColor = new Color(red, green, blue, MIN_ALPHA_VALUE);
		setOpaque(false);
		initializeTimers();
	}

	private void initializeTimers() {
		fadeInTimer = new Timer(FADE_IN_TIMER_DELAY, arg0 -> {
			int newAlpha = MAX_ALPHA_VALUE / (MAX_FADE_IN_TIME / FADE_IN_TIMER_DELAY) + maskColor.getAlpha();
			if (newAlpha > MAX_ALPHA_VALUE) {
				newAlpha = MAX_ALPHA_VALUE;
				fadeInTimer.stop();
				isFadingOut = false;
			}
			setMaskColor(new Color(red, green, blue, newAlpha));
			elapsedTime += FADE_IN_TIMER_DELAY;
			checkFadeInTimer();
		});
		fadeOutTimer = new Timer(FADE_OUT_TIMER_DELAY, arg0 -> {
			int newAlpha = maskColor.getAlpha() - MAX_ALPHA_VALUE / (MAX_FADE_OUT_TIME / FADE_OUT_TIMER_DELAY);
			if (newAlpha < MIN_ALPHA_VALUE) {
				newAlpha = MIN_ALPHA_VALUE;
				fadeOutTimer.stop();
				isFadingOut = false;
			}
			setMaskColor(new Color(red, green, blue, newAlpha));
			elapsedTime += FADE_IN_TIMER_DELAY;
			checkFadeOutTimer();
		});
		animationDelayTimer = new Timer(delayAnimationTime, arg0 -> {
			playAnimation = true;
			playFadeInAnimation();
		});
	}

	public void setMaskColor(Color c) {
		maskColor = c;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(maskColor);
		g.fillRect(0, 0, width, height);
	}

	public void playFadeInAnimation() {
		if (hasDelay) {
			if (playAnimation) {
				stopAnimation();
				elapsedTime = 0;
				fadeInTimer.restart();
				isFadingIn = true;
			} else {
				animationDelayTimer.restart();
				delayTimerStarted = true;
			}
		} else {
			stopAnimation();
			elapsedTime = 0;
			fadeInTimer.restart();
			isFadingIn = true;
		}
	}

	public void playFadeOutAnimation() {
		stopAnimation();
		elapsedTime = 0;
		fadeOutTimer.restart();
		isFadingOut = true;
	}

	private void checkFadeInTimer() {
		if (elapsedTime > MAX_FADE_IN_TIME) {
			fadeInTimer.stop();
			isFadingIn = false;
			if (maskColor.getAlpha() != MAX_ALPHA_VALUE) {
				setMaskColor(new Color(red, green, blue, MAX_ALPHA_VALUE));
			}
		}
	}

	private void checkFadeOutTimer() {
		if (elapsedTime > MAX_FADE_OUT_TIME) {
			fadeOutTimer.stop();
			isFadingOut = false;
			if (maskColor.getAlpha() != MIN_ALPHA_VALUE) {
				setMaskColor(new Color(red, green, blue, MIN_ALPHA_VALUE));
			}
		}
	}

	public void stopAnimation() {
		if (isFadingIn) {
			fadeInTimer.stop();
			isFadingIn = false;
			setMaskColor(new Color(red, green, blue, MIN_ALPHA_VALUE));
		} else if (isFadingOut) {
			fadeOutTimer.stop();
			isFadingOut = false;
			maskColor = new Color(red, green, blue, MIN_ALPHA_VALUE);
		}
		elapsedTime = 0;
		animationDelayTimer.stop();
		playAnimation = false;
		delayTimerStarted = false;
	}

	public boolean isAnimationRunning() {
		return isFadingOut || isFadingIn || delayTimerStarted;
	}
}