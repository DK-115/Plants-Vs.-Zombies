package a9;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MinionAttack extends JPanel {

	private static final long serialVersionUID = 1L;
	private Timer timer;
	private ArrayList<Actor> actors; // Plants and zombies all go in here
	BufferedImage bananaImage; // Maybe these images should be in those classes, but easy to change here.
	BufferedImage appleImage; // The image for the apple, one of the fruits
	BufferedImage minionImage; // The image for the minion/"zombie"
	int numRows;
	int numCols;
	int cellSize;

	/**
	 * Setup the basic info for the example
	 */
	public MinionAttack() {
		super();

		// Define some quantities of the scene
		numRows = 5;
		numCols = 7;
		cellSize = 75;
		setPreferredSize(new Dimension(50 + numCols * cellSize, 50 + numRows * cellSize));

		// Store all the plants and zombies in here.
		actors = new ArrayList<>();

		// Load images, otherwise gives an error if images are not found
		try {
			bananaImage = ImageIO.read(new File("src/a9/Animal-Icons/banana1.png"));
			appleImage = ImageIO.read(new File("src/a9/Animal-Icons/apple1.png"));
			minionImage = ImageIO.read(new File("src/a9/Animal-Icons/minion1.png"));
		} catch (IOException e) {
			System.out.println("A file was not found");
			System.exit(0);
		}

		// The timer updates the game each time it goes.
		// Get the javax.swing Timer, not from util.
		timer = new Timer(30, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateGameFrame();
			}

		});
		timer.start();

	}

	/***
	 * Implement the paint method to draw the plants
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (Actor actor : actors) {
			actor.draw(g, 0);
			actor.drawHealthBar(g);
		}
	}

	/**
	 * Updates the internal status of the actor.
	 */
	public void updateActors() {
		for (Actor actor : actors) {
			actor.update();
		}
	}

	/**
	 * Has the plants and zombies (or, in my case, the fruit and minions)
	 * attack each other.
	 */
	public void activateAttack() {
		for (Actor actor : actors) {
			for (Actor other : actors) {
				actor.attack(other);
			}
		}
	}

	/**
	 * If the actors are still alive, they remain on the screen.
	 * Otherwise, they are removed from the screen.
	 */
	public void generateNextTurnActors() {
		ArrayList<Actor> nextTurnActors = new ArrayList<>();
		for (Actor actor : actors) {
			if (actor.isAlive()) {
				nextTurnActors.add(actor);
			} else {
				actor.removeAction(actors); // removing dead actors
			}
		}
		actors = nextTurnActors;
	}

	/**
	 * Checks whether or not the actors are colliding with each other.
	 */
	public void checkCollisions() {
		for (Actor actor : actors) {
			for (Actor other : actors) {
				actor.setCollisionStatus(other);
			}
		}
	}

	/**
	 * Has the actors move at a certain speed on the panel.
	 */
	public void moveActors() {
		for (Actor actor : actors) {
			actor.move();
		}
	}

	/**
	 * This method adds more fruit/plants to the panel at random spots in the panel.
	 * Yes, the plants can spawn anywhere.
	 */
	public void addMoreFruit() {
		// Declaring a new Random generator for the fruit
		Random randomGenerator = new Random();
		// I want there to be 2 different types of fruit
		int pickRandomPlant = randomGenerator.nextInt(2);
		// Specifying a random row on the panel.
		int row = randomGenerator.nextInt(numRows);
		// Specifying a random column on the panel.
		int col = randomGenerator.nextInt(numCols);
		// This random number generator controls how fast the fruit spawns.
		int fruitSpawnRate = randomGenerator.nextInt(100);
		//If the spawn rate exceeds 97, we check if there is space available.
		if (fruitSpawnRate > 97) {
			// We want to check if a space is not already occupied in the panel.
			// If not, then we add a fruit to a random space in the panel.
			if (isSpaceAvailable(row, col)) {
				/**
				 * If the number generated from the pickRandomPlant generator
				 * is 0, we spawn a banana. Otherwise, we spawn an apple.
				 */
				if (pickRandomPlant == 0) {
					Banana banana = new Banana(new Point2D.Double(25 + col * cellSize, 25 + row * cellSize),
							new Point2D.Double(bananaImage.getWidth(), bananaImage.getHeight()), bananaImage, 100, 3, 1);
					actors.add(banana);
				} else {
					Apple apple = new Apple(new Point2D.Double(25 + col * cellSize, 25 + row * cellSize),
							new Point2D.Double(appleImage.getWidth(), appleImage.getHeight()), appleImage, 100, 3, 10);
					actors.add(apple);
				}
			}
		}
	}

	/**
	 * This method checks if any two actors are overlapping each other or not.
	 * @param row
	 * @param col
	 * @return false if there is overlap, true if there is not overlap.
	 */
	private boolean isSpaceAvailable(int row, int col) {
		// Getting the exact x position
		int xPos = col * cellSize;
		// Getting the exact y position
		int yPos = row * cellSize;
		// Drawing a rectangle for the fruit actor
		Rectangle fruitRectangle = new Rectangle(xPos, yPos, cellSize, cellSize);
		// Going through each actor in the game panel
		for (Actor actor : actors) {
			// Getting the exact position of the actor
			Point2D.Double position = actor.getPosition();
			// Getting the image size of the actor (hitbox is pretty much the image size)
			Point2D.Double hitbox = actor.getHitbox();
//			g.drawRect((int)pos.getX(),(int) pos.getY() - 10, (int)box.getX(), 5);
			 // Now we draw a rectangle for the general actor to check for overlap.
			Rectangle actorRectangle = new Rectangle((int) position.getX(), (int) position.getY(), (int) hitbox.getX(), (int) hitbox.getY());
			// If there is overlap, we return false (no space available).
			// If not, we return true (space available). 
			if (fruitRectangle.intersects(actorRectangle)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method adds more minions/zombies to the panel. Unlike the fruit/plants,
	 * the minions are fixed to be added at only the furthest right side of the panel.
	 */
	public void addMoreMinions() {
		// Declaring a new random object
		Random randomGenerator = new Random();
		// Specifying a random row, like with the addMoreFruit method
		int row = randomGenerator.nextInt(numRows);
		// We want to control how fast the minions spawn.
		int minionSpawnRate = randomGenerator.nextInt(100);
		// If the minion spawn rate exceeds 98, then we spawn minions.
		if (minionSpawnRate > 98) {
			Minion minion = new Minion(new Point2D.Double(25 + numCols * cellSize, 25 + row * cellSize),
					new Point2D.Double(minionImage.getWidth(), minionImage.getHeight()), minionImage, 100, 50, -2, 30);
			actors.add(minion);
		}
	}

	/**
	 * Updating everything that happens during the game.
	 */
	public void updateGameFrame() {
		updateActors();
		activateAttack();
		generateNextTurnActors();
		checkCollisions();
		moveActors();
		addMoreFruit();
		addMoreMinions();
		repaint();
	}

	/**
	 * Make the game and run it
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Schedule a job for the event-dispatching thread:
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame app = new JFrame("Minion Attack Test");
				app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				MinionAttack panel = new MinionAttack();

				app.setContentPane(panel);
				app.pack();
				app.setVisible(true);
			}
		});
	}

}