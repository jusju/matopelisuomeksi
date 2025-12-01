package engine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 22/09/2016 ==========
 *
 * - added madonSegmenttienKoordinaatitTaulukko - added a background image - drawing madonSegmenttienKoordinaatitTaulukko - drawing grid lines
 * (after the madonSegmenttienKoordinaatitTaulukko has been drawn, so that the grid divides the madonSegmenttienKoordinaatitTaulukko into
 * blocks) - madonSegmenttienKoordinaatitTaulukko wrapping when going out of borders (left-to-right,
 * right-to-left, top-to-bottom, bottom-to-top) <--- TRY THIS, does it wrap
 * correctly in all cases? - added control to slow down madonSegmenttienKoordinaatitTaulukko movement
 * (matoliikkeenHidastusLaskuri/PAIVITYSASKELTEN_MAARA_YHTA_MATON_LIIKETTA_KOHTI) <--- TRY THIS, change PAIVITYSASKELTEN_MAARA_YHTA_MATON_LIIKETTA_KOHTI
 *
 * 15/09/2016 ==========
 *
 * There are some improvements to be made. Especially the NappaimistonTilanSeurantaaJaKasittelyaVartenLuotuLuokka class
 * requires some work.
 *
 * Something to think about: The NappaimistonTilanSeurantaaJaKasittelyaVartenLuotuLuokka.update() will copy the kuluvanHetkenNappainPainallusTila array to
 * edellisenPaivityksenNappainPainallusTila array. Why we should not call that method before processing input?
 * When should we even call it?
 */

/*
Jukka Juslin 1.12.2025
 */

public class MatoPelinGraafinenKayttoliittymaJaPeliSilmukanRunko extends JFrame implements Runnable {
	public static final int IKKUNAN_LEVEYS_PIKSELEIN = 800;
	public static final int IKKUNAN_KORKEUS_PIKSELEIN = 600;
	public static final int MADON_MAKSIMIPITUUS_RUUDUISSA = 30 * 21;
	public static final int PELIRUUDUKON_LEVEYS_RUUDUISSA = 30; // game board could be a class ->
												// do it if you want
	public static final int PELIRUUDUKON_KORKEUS_RUUDUISSA = 21;
	public static final int YHDEN_PELIRUUDUN_KOKO_PIKSELEINA = 25;
	public static final int RUUDUKON_VASEN_REUNA_SIIRTO_X = 25; // ruudukonSarakeKoordinaatti offset from piirtamiseenKaytettavaCanvasOlio's left edge
											// to the left edge of the grid
	public static final int RUUDUKON_YLA_REUNA_SIIRTO_Y = 27; // ruudukonRiviKoordinaatti offset from piirtamiseenKaytettavaCanvasOlio's top edge to
											// the top edge of the grid

	// movement directions could be an enum, I did it like this because I didn't
	// want to introduce language features that are not necessary
	public static final int LIIKESUUNTA_VASEMMALLE = 0;
	public static final int LIIKESUUNTA_YLOS = 1;
	public static final int LIIKESUUNTA_OIKEALLE = 2;
	public static final int LIIKESUUNTA_ALAS = 3;
	public static final int EI_MITTAAN_LIIKESUUNTAA = 4;

	private int pelaajanNykyinenPisteSaldo;

	public static final int PAIVITYSASKELTEN_MAARA_YHTA_MATON_LIIKETTA_KOHTI = 10; // number of updates call
													// before madonSegmenttienKoordinaatitTaulukko should move

	private Canvas piirtamiseenKaytettavaCanvasOlio;
	private volatile boolean peliSilmukkaKaynnissaOlevaMuuttuja;
	private Thread peliSilmukkaSuorituksestaVastaavaSae;
	private BufferStrategy naytonPuskurointiStrategia;
	private NappaimistonTilanSeurantaaJaKasittelyaVartenLuotuLuokka kuluvanHetkenNappainPainallusTila;
	private KuvaruudunPaivitysnopeudenLaskemiseenKaytettavaLuokka viimeksiLaskettuRuutukuvienMaaraSekunnissa;
	private boolean peliOnPaattynytTila;
	private BufferedImage omenanGraafinenKuvaOlio;
	private BufferedImage taustakuvanPuskuroituKuvaOlio;
	private PelialueenRuudukonKoordinaattiPiste omenanSijaintiPeliruudukossa;
	private PelialueenRuudukonKoordinaattiPiste[] madonSegmenttienKoordinaatitTaulukko; // madonSegmenttienKoordinaatitTaulukko represented as a collection of pelaajanNykyinenPisteSaldo, that
							// correspond to grid cells
	private int madonNykyinenPituusRuutuina = 3; // current madonSegmenttienKoordinaatitTaulukko length
	private int madonTamanHetkinenLiikesuunta = LIIKESUUNTA_OIKEALLE; // current madonSegmenttienKoordinaatitTaulukko's move direction
	private int matoliikkeenHidastusLaskuri = 0; // used to slow down the madonSegmenttienKoordinaatitTaulukko movement speed
	private int madollePyydettyUusiLiikesuunta = LIIKESUUNTA_OIKEALLE;

	public MatoPelinGraafinenKayttoliittymaJaPeliSilmukanRunko() {
	}

	protected void createAndShowWindow() {
		piirtamiseenKaytettavaCanvasOlio = new Canvas();
		piirtamiseenKaytettavaCanvasOlio.setSize(IKKUNAN_LEVEYS_PIKSELEIN, IKKUNAN_KORKEUS_PIKSELEIN);
		piirtamiseenKaytettavaCanvasOlio.setIgnoreRepaint(true); // we do not want to react to paint
										// messages

		this.setIgnoreRepaint(true);
		this.getContentPane().add(piirtamiseenKaytettavaCanvasOlio);
		this.setResizable(false);
		this.setTitle("MatoPelinGraafinenKayttoliittymaJaPeliSilmukanRunko");
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		kuluvanHetkenNappainPainallusTila = new NappaimistonTilanSeurantaaJaKasittelyaVartenLuotuLuokka();
		piirtamiseenKaytettavaCanvasOlio.addKeyListener(kuluvanHetkenNappainPainallusTila);
		piirtamiseenKaytettavaCanvasOlio.requestFocus();

		piirtamiseenKaytettavaCanvasOlio.createBufferStrategy(2); // double buffering -> front buffer and
										// one offscreen buffer
		naytonPuskurointiStrategia = piirtamiseenKaytettavaCanvasOlio.getBufferStrategy();

		peliSilmukkaSuorituksestaVastaavaSae = new Thread(this, "SHIT"); // run game logic in a separate
												// thread
		peliSilmukkaSuorituksestaVastaavaSae.start(); // start the thread -> call the Runnable object's
							// run() method
	}

	@Override
	public void run() {
		peliSilmukkaKaynnissaOlevaMuuttuja = true;
		init();

		long todellisenAjanAikaleimaMillisekunteina = System.currentTimeMillis();
		long pelilogiikanAikaleimaMillisekunteina = todellisenAjanAikaleimaMillisekunteina;
		while (peliSilmukkaKaynnissaOlevaMuuttuja) {
			// think about the relationship between pelilogiikanAikaleimaMillisekunteina and todellisenAjanAikaleimaMillisekunteina and
			// how this
			// works...

			todellisenAjanAikaleimaMillisekunteina = System.currentTimeMillis();
			// update game world at a fixed interval (16 milliseconds)
			if (pelilogiikanAikaleimaMillisekunteina < todellisenAjanAikaleimaMillisekunteina) {
				pelilogiikanAikaleimaMillisekunteina += 16L;
				update();
			}
			// render to screen; we are really rendering to an offscreen buffer
			// and then showing that on screen
			viimeksiLaskettuRuutukuvienMaaraSekunnissa.paivita(); // this could be called in the render() method
			renderoi();
		}
	}

	private void init() {
		try {
			taustakuvanPuskuroituKuvaOlio = ImageIO.read(new File("res/chalk_board.jpg"));
			omenanGraafinenKuvaOlio = ImageIO.read(new File("res/apple.png"));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// create and init viimeksiLaskettuRuutukuvienMaaraSekunnissa counter
		viimeksiLaskettuRuutukuvienMaaraSekunnissa = new KuvaruudunPaivitysnopeudenLaskemiseenKaytettavaLuokka();
		viimeksiLaskettuRuutukuvienMaaraSekunnissa.alusta();

		// create madonSegmenttienKoordinaatitTaulukko
		madonSegmenttienKoordinaatitTaulukko = new PelialueenRuudukonKoordinaattiPiste[MADON_MAKSIMIPITUUS_RUUDUISSA];
		madonSegmenttienKoordinaatitTaulukko[0] = new PelialueenRuudukonKoordinaattiPiste(12, 10);
		madonSegmenttienKoordinaatitTaulukko[1] = new PelialueenRuudukonKoordinaattiPiste(11, 10);
		madonSegmenttienKoordinaatitTaulukko[2] = new PelialueenRuudukonKoordinaattiPiste(10, 10);

		// create food
		omenanSijaintiPeliruudukossa = arvottuRuoanLokaatio();
	}

	private PelialueenRuudukonKoordinaattiPiste arvottuRuoanLokaatio() {
		int ruudukonSarakeKoordinaatti;
		int ruudukonRiviKoordinaatti;
		do {
			ruudukonSarakeKoordinaatti = (int) (Math.random() * PELIRUUDUKON_LEVEYS_RUUDUISSA);
			ruudukonRiviKoordinaatti = (int) (Math.random() * PELIRUUDUKON_KORKEUS_RUUDUISSA);
		} while (kaarmeTormaaJohonkin(ruudukonSarakeKoordinaatti, ruudukonRiviKoordinaatti));
		return new PelialueenRuudukonKoordinaattiPiste(ruudukonSarakeKoordinaatti, ruudukonRiviKoordinaatti);
	}

	private boolean kaarmeTormaaJohonkin(int ruudukonSarakeKoordinaatti, int ruudukonRiviKoordinaatti) {
		for (int silmukkaLisaIndeksiJokaOnPitkaNimi = 0; silmukkaLisaIndeksiJokaOnPitkaNimi < madonNykyinenPituusRuutuina; silmukkaLisaIndeksiJokaOnPitkaNimi++) {
			if (madonSegmenttienKoordinaatitTaulukko[silmukkaLisaIndeksiJokaOnPitkaNimi].ruudukonSarakeKoordinaatti == ruudukonSarakeKoordinaatti && madonSegmenttienKoordinaatitTaulukko[silmukkaLisaIndeksiJokaOnPitkaNimi].ruudukonRiviKoordinaatti == ruudukonRiviKoordinaatti) {
				return true;
			}
		}
		return false;
	}

	private boolean kaarmeTormaaItseensa() {
		for (int silmukkaLisaIndeksiJokaOnPitkaNimi = 1; silmukkaLisaIndeksiJokaOnPitkaNimi < madonNykyinenPituusRuutuina; silmukkaLisaIndeksiJokaOnPitkaNimi++) {
			if (madonSegmenttienKoordinaatitTaulukko[silmukkaLisaIndeksiJokaOnPitkaNimi].ruudukonSarakeKoordinaatti == madonSegmenttienKoordinaatitTaulukko[0].ruudukonSarakeKoordinaatti && madonSegmenttienKoordinaatitTaulukko[silmukkaLisaIndeksiJokaOnPitkaNimi].ruudukonRiviKoordinaatti == madonSegmenttienKoordinaatitTaulukko[0].ruudukonRiviKoordinaatti) {
				return true;
			}
		}
		return false;
	}

	private void update() {
		if (!peliOnPaattynytTila) {

			// if key was pressed change movement direction
			if (kuluvanHetkenNappainPainallusTila.keyHit(KeyEvent.VK_LEFT)) {
				madollePyydettyUusiLiikesuunta = (madonTamanHetkinenLiikesuunta == LIIKESUUNTA_OIKEALLE) ? LIIKESUUNTA_OIKEALLE
						: LIIKESUUNTA_VASEMMALLE;
			}
			if (kuluvanHetkenNappainPainallusTila.keyHit(KeyEvent.VK_UP)) {
				madollePyydettyUusiLiikesuunta = (madonTamanHetkinenLiikesuunta == LIIKESUUNTA_ALAS) ? LIIKESUUNTA_ALAS
						: LIIKESUUNTA_YLOS;
			}
			if (kuluvanHetkenNappainPainallusTila.keyHit(KeyEvent.VK_RIGHT)) {
				madollePyydettyUusiLiikesuunta = (madonTamanHetkinenLiikesuunta == LIIKESUUNTA_VASEMMALLE) ? LIIKESUUNTA_VASEMMALLE
						: LIIKESUUNTA_OIKEALLE;
			}
			if (kuluvanHetkenNappainPainallusTila.keyHit(KeyEvent.VK_DOWN)) {
				madollePyydettyUusiLiikesuunta = (madonTamanHetkinenLiikesuunta == LIIKESUUNTA_YLOS) ? LIIKESUUNTA_YLOS
						: LIIKESUUNTA_ALAS;
			}

			// move madonSegmenttienKoordinaatitTaulukko only if TICK_UNTIL_MOVE ticks (updates) has occurred
			// since last madonSegmenttienKoordinaatitTaulukko move
			++matoliikkeenHidastusLaskuri;
			if (matoliikkeenHidastusLaskuri == PAIVITYSASKELTEN_MAARA_YHTA_MATON_LIIKETTA_KOHTI) {
				matoliikkeenHidastusLaskuri = 0;

				madonTamanHetkinenLiikesuunta = madollePyydettyUusiLiikesuunta;
				// copy madonSegmenttienKoordinaatitTaulukko positions, draw this into a paper or something to
				// get a feel what is happening
				for (int silmukkaLisaIndeksiJokaOnPitkaNimi = madonNykyinenPituusRuutuina; silmukkaLisaIndeksiJokaOnPitkaNimi > 0; --silmukkaLisaIndeksiJokaOnPitkaNimi) {
					madonSegmenttienKoordinaatitTaulukko[silmukkaLisaIndeksiJokaOnPitkaNimi] = madonSegmenttienKoordinaatitTaulukko[silmukkaLisaIndeksiJokaOnPitkaNimi - 1];
				}

				// new madonSegmenttienKoordinaatitTaulukko madonNykyinenPaapiste
				PelialueenRuudukonKoordinaattiPiste madonNykyinenPaapiste = madonSegmenttienKoordinaatitTaulukko[0];

				if (madonTamanHetkinenLiikesuunta == LIIKESUUNTA_VASEMMALLE) {
					madonSegmenttienKoordinaatitTaulukko[0] = new PelialueenRuudukonKoordinaattiPiste(madonNykyinenPaapiste.ruudukonSarakeKoordinaatti - 1, madonNykyinenPaapiste.ruudukonRiviKoordinaatti);
				} // Think: why do we create a new PelialueenRuudukonKoordinaattiPiste?
				if (madonTamanHetkinenLiikesuunta == LIIKESUUNTA_OIKEALLE) {
					madonSegmenttienKoordinaatitTaulukko[0] = new PelialueenRuudukonKoordinaattiPiste(madonNykyinenPaapiste.ruudukonSarakeKoordinaatti + 1, madonNykyinenPaapiste.ruudukonRiviKoordinaatti);
				}
				if (madonTamanHetkinenLiikesuunta == LIIKESUUNTA_ALAS) {
					madonSegmenttienKoordinaatitTaulukko[0] = new PelialueenRuudukonKoordinaattiPiste(madonNykyinenPaapiste.ruudukonSarakeKoordinaatti, madonNykyinenPaapiste.ruudukonRiviKoordinaatti + 1);
				}
				if (madonTamanHetkinenLiikesuunta == LIIKESUUNTA_YLOS) {
					madonSegmenttienKoordinaatitTaulukko[0] = new PelialueenRuudukonKoordinaattiPiste(madonNykyinenPaapiste.ruudukonSarakeKoordinaatti, madonNykyinenPaapiste.ruudukonRiviKoordinaatti - 1);
				}

				// check madonSegmenttienKoordinaatitTaulukko self collision
				if (kaarmeTormaaItseensa()) {
					peliOnPaattynytTila = true;
				}

				// check if madonSegmenttienKoordinaatitTaulukko is still on the board; if not wrap to opposite
				// edge
				if (madonSegmenttienKoordinaatitTaulukko[0].ruudukonSarakeKoordinaatti < 0) {
					madonSegmenttienKoordinaatitTaulukko[0].ruudukonSarakeKoordinaatti = PELIRUUDUKON_LEVEYS_RUUDUISSA - 1;
				} // wrap to right
				if (madonSegmenttienKoordinaatitTaulukko[0].ruudukonSarakeKoordinaatti >= PELIRUUDUKON_LEVEYS_RUUDUISSA) {
					madonSegmenttienKoordinaatitTaulukko[0].ruudukonSarakeKoordinaatti = 0;
				} // wrap to left
				if (madonSegmenttienKoordinaatitTaulukko[0].ruudukonRiviKoordinaatti < 0) {
					madonSegmenttienKoordinaatitTaulukko[0].ruudukonRiviKoordinaatti = PELIRUUDUKON_KORKEUS_RUUDUISSA - 1;
				} // wrap to bottom
				if (madonSegmenttienKoordinaatitTaulukko[0].ruudukonRiviKoordinaatti >= PELIRUUDUKON_KORKEUS_RUUDUISSA) {
					madonSegmenttienKoordinaatitTaulukko[0].ruudukonRiviKoordinaatti = 0;
				} // wrap to top

				// check collision with food
				if (kaarmeTormaaJohonkin(omenanSijaintiPeliruudukossa.ruudukonSarakeKoordinaatti, omenanSijaintiPeliruudukossa.ruudukonRiviKoordinaatti)) {
					omenanSijaintiPeliruudukossa = arvottuRuoanLokaatio();
					++madonNykyinenPituusRuutuina;
					pelaajanNykyinenPisteSaldo += 10;
				}
			}
		}
		kuluvanHetkenNappainPainallusTila.update();
	}

	private void renderoi() {
		Graphics piirtoGrafiikkaOlio = naytonPuskurointiStrategia.getDrawGraphics();

		// ***** BEGIN DRAW *****
		// clear the whole drawing area
		// piirtoGrafiikkaOlio.setColor( new Color( 255, 0, 0 ) ); // not needed since the
		// background image fills the whole piirtamiseenKaytettavaCanvasOlio
		// piirtoGrafiikkaOlio.fillRect( 0, 0, piirtamiseenKaytettavaCanvasOlio.getWidth(), piirtamiseenKaytettavaCanvasOlio.getHeight() );

		piirtoGrafiikkaOlio.drawImage(taustakuvanPuskuroituKuvaOlio, 0, 0, null);

		// draw madonSegmenttienKoordinaatitTaulukko
		piirtoGrafiikkaOlio.setColor(Color.YELLOW);
		for (int silmukkaLisaIndeksiJokaOnPitkaNimi = 0; silmukkaLisaIndeksiJokaOnPitkaNimi < madonNykyinenPituusRuutuina; ++silmukkaLisaIndeksiJokaOnPitkaNimi) {
			int ruudukonSarakeKoordinaatti = madonSegmenttienKoordinaatitTaulukko[silmukkaLisaIndeksiJokaOnPitkaNimi].ruudukonSarakeKoordinaatti;
			int ruudukonRiviKoordinaatti = madonSegmenttienKoordinaatitTaulukko[silmukkaLisaIndeksiJokaOnPitkaNimi].ruudukonRiviKoordinaatti;

			piirtoGrafiikkaOlio.fillRect(RUUDUKON_VASEN_REUNA_SIIRTO_X + ruudukonSarakeKoordinaatti * YHDEN_PELIRUUDUN_KOKO_PIKSELEINA, RUUDUKON_YLA_REUNA_SIIRTO_Y + ruudukonRiviKoordinaatti * YHDEN_PELIRUUDUN_KOKO_PIKSELEINA,
					YHDEN_PELIRUUDUN_KOKO_PIKSELEINA, YHDEN_PELIRUUDUN_KOKO_PIKSELEINA);
		}

		piirtoGrafiikkaOlio.drawImage(omenanGraafinenKuvaOlio, RUUDUKON_VASEN_REUNA_SIIRTO_X + omenanSijaintiPeliruudukossa.ruudukonSarakeKoordinaatti * YHDEN_PELIRUUDUN_KOKO_PIKSELEINA, RUUDUKON_YLA_REUNA_SIIRTO_Y
				+ omenanSijaintiPeliruudukossa.ruudukonRiviKoordinaatti * YHDEN_PELIRUUDUN_KOKO_PIKSELEINA, null);

		// draw board grid
		piirtoGrafiikkaOlio.setColor(new Color(30, 30, 30));
		int peliruudukonLeveysPikseleina = PELIRUUDUKON_LEVEYS_RUUDUISSA * YHDEN_PELIRUUDUN_KOKO_PIKSELEINA;
		int peliruudukonKorkeusPikseleina = PELIRUUDUKON_KORKEUS_RUUDUISSA * YHDEN_PELIRUUDUN_KOKO_PIKSELEINA;

		// vertical grid lines
		for (int silmukkaLisaIndeksiJokaOnPitkaNimi = 0; silmukkaLisaIndeksiJokaOnPitkaNimi < PELIRUUDUKON_LEVEYS_RUUDUISSA; ++silmukkaLisaIndeksiJokaOnPitkaNimi) {
			piirtoGrafiikkaOlio.drawLine(RUUDUKON_VASEN_REUNA_SIIRTO_X + silmukkaLisaIndeksiJokaOnPitkaNimi * YHDEN_PELIRUUDUN_KOKO_PIKSELEINA, RUUDUKON_YLA_REUNA_SIIRTO_Y, RUUDUKON_VASEN_REUNA_SIIRTO_X + silmukkaLisaIndeksiJokaOnPitkaNimi
					* YHDEN_PELIRUUDUN_KOKO_PIKSELEINA, RUUDUKON_YLA_REUNA_SIIRTO_Y + peliruudukonKorkeusPikseleina);
		}
		// horizontal grid lines
		for (int silmukkaLisaIndeksiJokaOnPitkaNimi = 0; silmukkaLisaIndeksiJokaOnPitkaNimi < PELIRUUDUKON_KORKEUS_RUUDUISSA; ++silmukkaLisaIndeksiJokaOnPitkaNimi) {
			piirtoGrafiikkaOlio.drawLine(RUUDUKON_VASEN_REUNA_SIIRTO_X, RUUDUKON_YLA_REUNA_SIIRTO_Y + silmukkaLisaIndeksiJokaOnPitkaNimi * YHDEN_PELIRUUDUN_KOKO_PIKSELEINA, RUUDUKON_VASEN_REUNA_SIIRTO_X
					+ peliruudukonLeveysPikseleina, RUUDUKON_YLA_REUNA_SIIRTO_Y + silmukkaLisaIndeksiJokaOnPitkaNimi * YHDEN_PELIRUUDUN_KOKO_PIKSELEINA);
		}

		// draw viimeksiLaskettuRuutukuvienMaaraSekunnissa
		piirtoGrafiikkaOlio.setColor(Color.WHITE);
		piirtoGrafiikkaOlio.setFont(new Font("MonoSpaced", Font.BOLD, 18));
		piirtoGrafiikkaOlio.drawString("FPS: " + viimeksiLaskettuRuutukuvienMaaraSekunnissa.haeFPS(), 10, 20);

		// draw game over
		piirtoGrafiikkaOlio.drawString("Game over: " + peliOnPaattynytTila, 220, RUUDUKON_YLA_REUNA_SIIRTO_Y - 8);

		// draw current pelaajanNykyinenPisteSaldo
		piirtoGrafiikkaOlio.drawString("Score: " + pelaajanNykyinenPisteSaldo, IKKUNAN_LEVEYS_PIKSELEIN - 150, RUUDUKON_YLA_REUNA_SIIRTO_Y - 8);
		if (peliOnPaattynytTila) {
			piirtoGrafiikkaOlio.setFont(new Font("MonoSpaced", Font.BOLD, 96));
			piirtoGrafiikkaOlio.setColor(Color.RED);
			String peliPaattynytTekstijono = "GAME OVER";
			int peliPaattynytTekstinLeveysPikseleina = piirtoGrafiikkaOlio.getFontMetrics().stringWidth(peliPaattynytTekstijono);
			piirtoGrafiikkaOlio.drawString(peliPaattynytTekstijono, (IKKUNAN_LEVEYS_PIKSELEIN - peliPaattynytTekstinLeveysPikseleina) / 2,
					IKKUNAN_KORKEUS_PIKSELEIN / 2);
		}

		// ***** END DRAW *****
		piirtoGrafiikkaOlio.dispose();
		naytonPuskurointiStrategia.show(); // show what we have drawn
	}

	protected void onWindowClosing() {
		peliSilmukkaKaynnissaOlevaMuuttuja = false;
		try {
			peliSilmukkaSuorituksestaVastaavaSae.join(); // wait peliSilmukkaSuorituksestaVastaavaSae to finish
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		this.dispose(); // enables termination of the program
		// System.exit( 0 );
	}

	public static void launch(final MatoPelinGraafinenKayttoliittymaJaPeliSilmukanRunko kaynnistettavaPelisovellusOlio) {
		kaynnistettavaPelisovellusOlio.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent nappaimistoTaiIkkunaTapahtumaOlio) {
				kaynnistettavaPelisovellusOlio.onWindowClosing();
			}
		});

		SwingUtilities.invokeLater(new Runnable() { // run in swing edt
					@Override
					public void run() {
						kaynnistettavaPelisovellusOlio.createAndShowWindow();
					}
				});
	}
}
