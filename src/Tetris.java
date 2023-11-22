package src;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Tetris extends JPanel {

	Pieces piece = new Pieces();

	private static final long serialVersionUID = -8715353373678321308L;
	private final int BOARD_WIDTH = 12;
	private final int BOARD_HEIGHT = 23;
	private final int DELAY = 1000;

	private Point pieceOrigin;
	private int currentPiece;
	private int rotation;
	private ArrayList<Integer> nextPieces = new ArrayList<>();

	private long score;
	private Color[][] well;

	private void init() {
		well = new Color[BOARD_WIDTH][BOARD_HEIGHT];
		// Criação do tabuleiro
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 23; j++) {
				if (i == 0 || i == 11 || j == 22) {
					well[i][j] = Color.GRAY;
				} else {
					well[i][j] = Color.BLACK;
				}
			}
		}
		newPiece();
	}
	private void startGame() {
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(DELAY);
					dropDown();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void newPiece() {
		// Geração de nova peça
		pieceOrigin = new Point(5, 2);
		rotation = 0;
		if (nextPieces.isEmpty()) {
			Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6);
			Collections.shuffle(nextPieces);
		}
		currentPiece = nextPieces.get(0);
		nextPieces.remove(0);
	}

	private boolean collidesAt(int x, int y, int rotation) {
		// Verificação de colisão
		for (Point p : piece.Tetraminos[currentPiece][rotation]) {
			if (well[p.x + x][p.y + y] != Color.BLACK) {
				return true;
			}
		}
		return false;
	}

	public void rotate(int i) {
		// Lógica de rotação
		int newRotation = (rotation + i) % 4;
		if (newRotation < 0) {
			newRotation = 3;
		}
		if (!collidesAt(pieceOrigin.x, pieceOrigin.y, newRotation)) {
			rotation = newRotation;
		}
		repaint();
	}

	public void move(int i) {
		// Lógica de movimento
		if (!collidesAt(pieceOrigin.x + i, pieceOrigin.y, rotation)) {
			pieceOrigin.x += i;
		}
		repaint();
	}

	public void dropDown() {
		// Lógica de queda da peça
		if (!collidesAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
			pieceOrigin.y += 1;
		} else {
			fixToWell();
		}
		repaint();
	}

	public void fixToWell() {
		// Fixação da peça no tabuleiro
		for (Point p : piece.Tetraminos[currentPiece][rotation]) {
			well[pieceOrigin.x + p.x][pieceOrigin.y + p.y] = piece.Colors[currentPiece];
		}
		clearRows();
		newPiece();
	}

	public void deleteRow(int row) {
		// Deleta linha do tabuleiro
		for (int j = row-1; j > 0; j--) {
			for (int i = 1; i < 11; i++) {
				well[i][j+1] = well[i][j];
			}
		}
	}

	public void clearRows() {
		// Lógica de limpeza de linhas
		boolean gap;
		int numClears = 0;

		for (int j = 21; j > 0; j--) {
			gap = false;
			for (int i = 1; i < 11; i++) {
				if (well[i][j] == Color.BLACK) {
					gap = true;
					break;
				}
			}
			if (!gap) {
				deleteRow(j);
				j += 1;
				numClears += 1;
			}
		}

		switch (numClears) {
			case 1:
				score += 100;
				break;
			case 2:
				score += 300;
				break;
			case 3:
				score += 500;
				break;
			case 4:
				score += 800;
				break;
		}
	}

	private void drawPiece(Graphics g) {
		// Desenha a peça atual
		g.setColor(piece.Colors[currentPiece]);
		for (Point p : piece.Tetraminos[currentPiece][rotation]) {
			g.fillRect((p.x + pieceOrigin.x) * 26,
					(p.y + pieceOrigin.y) * 26,
					25, 25);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		// Método de desenho do componente
		g.fillRect(0, 0, 26*12, 26*23);
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 23; j++) {
				g.setColor(well[i][j]);
				g.fillRect(26*i, 26*j, 25, 25);
			}
		}

		// Display da pontuação
		g.setColor(Color.WHITE);
		g.drawString("" + score, 19*12, 25);

		// Desenhe a peça que está caindo
		drawPiece(g);
	}

	public static void main(String[] args) {
		JFrame f = new JFrame("Tetris");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(12*26+10, 26*23+25);
		f.setVisible(true);

		final Tetris game = new Tetris();
		game.init();
		f.add(game);

		// Keyboard controls
		f.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
						game.rotate(-1);
						break;
					case KeyEvent.VK_DOWN:
						game.rotate(+1);
						break;
					case KeyEvent.VK_LEFT:
						game.move(-1);
						break;
					case KeyEvent.VK_RIGHT:
						game.move(+1);
						break;
					case KeyEvent.VK_SPACE:
						game.dropDown();
						game.score += 1;
						break;
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		// Make the falling piece drop every second
		new Thread() {
			@Override public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						game.dropDown();
					} catch ( InterruptedException e ) {}
				}
			}
		}.start();
	}
}
