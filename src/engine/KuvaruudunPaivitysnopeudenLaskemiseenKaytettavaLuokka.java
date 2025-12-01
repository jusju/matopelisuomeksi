package engine;

/**
 * Keeps track of call frequency of this class's update() method.
 * Used to calculate frame rate -> update() method is called once
 * every renderer frame (image).
 */


/*
Jukka Juslin 1.12.2025
 */

public class KuvaruudunPaivitysnopeudenLaskemiseenKaytettavaLuokka {
    private long edellisenPaivityksenAikaleimaMillisekunteina;
    private int kuluvanSekunninRuutukuvienLaskuri;
    private int viimeksiLaskettuRuutukuvienMaaraSekunnissa;
    private long kertyneidenMillisekuntienSumma;     // time accumulator

    public KuvaruudunPaivitysnopeudenLaskemiseenKaytettavaLuokka() { }

    public void alusta() {
        edellisenPaivityksenAikaleimaMillisekunteina = System.currentTimeMillis();
    }

    // call this once every time the method whose frequency you want
    // to keep track of is called
    public void paivita() {
        ++kuluvanSekunninRuutukuvienLaskuri;
        long nykyinenAikaleimaMillisekunteina = System.currentTimeMillis();
        kertyneidenMillisekuntienSumma += (nykyinenAikaleimaMillisekunteina - edellisenPaivityksenAikaleimaMillisekunteina);
        edellisenPaivityksenAikaleimaMillisekunteina = nykyinenAikaleimaMillisekunteina;
        if ( kertyneidenMillisekuntienSumma > 1000 ) {
            viimeksiLaskettuRuutukuvienMaaraSekunnissa = kuluvanSekunninRuutukuvienLaskuri;
            kuluvanSekunninRuutukuvienLaskuri = 0;
            kertyneidenMillisekuntienSumma -= 1000;
        }
    }

    public int haeFPS() {
        return viimeksiLaskettuRuutukuvienMaaraSekunnissa;
    }
}
