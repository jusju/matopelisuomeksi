package engine;

// This actually is useless class, since Java API contains
// a class called PelialueenRuudukonKoordinaattiPiste that provides the functionality we need.
// But I kept it here anyway.

/*
Jukka Juslin 1.12.2025
 */
public class PelialueenRuudukonKoordinaattiPiste {
    public int ruudukonSarakeKoordinaatti, ruudukonRiviKoordinaatti;        // no place for data hiding, or encapsulation here -> no useless getters/setters

    public PelialueenRuudukonKoordinaattiPiste( int ruudukonSarakeKoordinaatti, int ruudukonRiviKoordinaatti ) {
        this.ruudukonSarakeKoordinaatti = ruudukonSarakeKoordinaatti;
        this.ruudukonRiviKoordinaatti = ruudukonRiviKoordinaatti;
    }

    public PelialueenRuudukonKoordinaattiPiste( PelialueenRuudukonKoordinaattiPiste kopioitavaPisteOlio ) {       // copy constructor
        this.ruudukonSarakeKoordinaatti = kopioitavaPisteOlio.ruudukonSarakeKoordinaatti;
        this.ruudukonRiviKoordinaatti = kopioitavaPisteOlio.ruudukonRiviKoordinaatti;
    }
}
