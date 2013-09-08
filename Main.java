import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JFrame;


public class Main extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;
	private static int width = 960;
	private static int height = width / 16 * 9;
	private static int scale = 1;
	public static String title = "Paintball 0.0.6";
	private Thread thread;
	private JFrame frame;
	private static Keyboard key;
	private Level level;
	public Player player;
	private static boolean running = false;
	private Screen screen;
	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	boolean alive = true;
	
	public Main() {
		Dimension size = new Dimension(width * scale, height * scale);
		setPreferredSize(size);

		screen = new Screen(width, height);
		frame = new JFrame();
		key = new Keyboard();
		level = new SpawnLevel("/textures/levels/level_1.png");
		TileCoordinate playerSpawn = new TileCoordinate(40, 32);
		player = new Player(playerSpawn.x(), playerSpawn.y(), key);
		player.init(level);
		Thread t1 = new Thread();
		t1.start();

		addKeyListener(key);

		Mouse mouse = new Mouse();
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
	}

	public static int getWindowWidth() {
		return width * scale;
	}

	public static int getWindowHeight() {
		return height * scale;
	}

	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Display");
		thread.start();
	}

	public synchronized void stop() {
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void run() {
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 60.0;
		double delta = 0;
		int frames = 0;
		int updates = 0;
		requestFocus();
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				update();
				updates++;
				delta--;
			}
			render();
			frames++;

			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				frame.setTitle(title + "  |  " + "FPS: " + frames + " | " + "UPS: " + updates);
				updates = 0;
				frames = 0;

			}

		}
		stop();
	}

	public void update() {
		key.update();
		player.update();
		level.update();
	}

	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		screen.clear();
		int xScroll = player.x - screen.width / 2;
		int yScroll = player.y - screen.height / 2;
		level.render(xScroll, yScroll, screen);
		player.render(screen);
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = screen.pixels[i];
		}
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		g.setFont(new Font("Verdana", 0, 16));
		g.setColor(Color.RED);
		g.drawString("PRE-ALPHA", 860, 505);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Verdana", 0, 12));
		g.drawString("X: " + player.x / 16, 14, 34);
		g.drawString("Y: " + player.y / 16, 14, 46);
		g.drawString("Press ESC to exit.", 14, 22);
		g.setColor(Color.BLACK);
		if (key.exit) {
			System.exit(0);
		}
		bs.show();

		/*if (manaWidth <= 125) {
			manaCounter++;
			if (manaCounter >= 80) {
				manaWidth += 12.5;
				mana += 1;
				if (manaCounter == 80) {
					manaCounter = 0;
				}
				if (manaWidth >= 125) {
					manaWidth = 125;
				}
				if (mana > 0) {
					Player.fireRate = 25;
				}
				if (mana >= 10) {
					mana = 10;
				}
				if (manaWidth <= 0) {
					manaWidth = 0;
				}
			}
		}
		if (health > 0) {
			healthCounter++;
		}
		if (healthCounter >= 50) {
			healthWidth += 12.5;
			healthCounter = 0;
		}
		if (healthWidth >= 125) {
			healthWidth = 125;
		}*/
	}

	public static void main(String[] args) {
		Main game = new Main();
		game.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		game.frame.setAlwaysOnTop(false);
		game.frame.setResizable(false);
		game.frame.setTitle(title);
		game.frame.add(game);
		game.frame.pack();
		game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game.frame.setLocationRelativeTo(null);
		game.frame.setVisible(true);
		game.frame.setSize(width, height);

		game.start();
	}
}
