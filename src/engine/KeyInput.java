package engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class NappaimistonTilanSeurantaaJaKasittelyaVartenLuotuLuokka implements KeyListener {
    public static final int NAPPAYSTENKOKONAISMAARA_TAULUKOSSA = 256;

    private boolean kuluvanHetkenNappainPainallusTila[];
    private boolean edellisenPaivityksenNappainPainallusTila[];

    public NappaimistonTilanSeurantaaJaKasittelyaVartenLuotuLuokka() {
        kuluvanHetkenNappainPainallusTila = new boolean[NAPPAYSTENKOKONAISMAARA_TAULUKOSSA];
        edellisenPaivityksenNappainPainallusTila = new boolean[NAPPAYSTENKOKONAISMAARA_TAULUKOSSA];
    }

    public synchronized void update() {
        for ( int silmukkaLisaIndeksiJokaOnPitkaNimi = 0; silmukkaLisaIndeksiJokaOnPitkaNimi < NAPPAYSTENKOKONAISMAARA_TAULUKOSSA; ++silmukkaLisaIndeksiJokaOnPitkaNimi ) {
            edellisenPaivityksenNappainPainallusTila[silmukkaLisaIndeksiJokaOnPitkaNimi] = kuluvanHetkenNappainPainallusTila[silmukkaLisaIndeksiJokaOnPitkaNimi];
        }
    }

    public synchronized boolean keyHit( int nappaimenKoodiarvo ) {
        return kuluvanHetkenNappainPainallusTila[nappaimenKoodiarvo] && !edellisenPaivityksenNappainPainallusTila[nappaimenKoodiarvo];
    }

    public synchronized boolean keyDown( int nappaimenKoodiarvo ) {
        // KeyEvent.VK_SPACE]
        return kuluvanHetkenNappainPainallusTila[nappaimenKoodiarvo];
    }

    public void keyPressed( KeyEvent nappaimistoTaiIkkunaTapahtumaOlio ) {
        int nappaimenKoodiarvo = nappaimistoTaiIkkunaTapahtumaOlio.getKeyCode();
        if ( nappaimenKoodiarvo >= 0 && nappaimenKoodiarvo < NAPPAYSTENKOKONAISMAARA_TAULUKOSSA ) {
            kuluvanHetkenNappainPainallusTila[nappaimenKoodiarvo] = true;
        }
    }

    public synchronized void keyReleased( KeyEvent nappaimistoTaiIkkunaTapahtumaOlio ) {
        int nappaimenKoodiarvo = nappaimistoTaiIkkunaTapahtumaOlio.getKeyCode();
        if ( nappaimenKoodiarvo >= 0 && nappaimenKoodiarvo < NAPPAYSTENKOKONAISMAARA_TAULUKOSSA ) {
            kuluvanHetkenNappainPainallusTila[nappaimenKoodiarvo] = false;
        }
    }

    public void keyTyped( KeyEvent nappaimistoTaiIkkunaTapahtumaOlio ) {
// no need for this
    }
}
