package engine;

/**
 * Keeps track of call frequency of this class's update() method.
 * Used to calculate frame rate -> update() method is called once
 * every renderer frame (image).
 */
public class KuvaruudunPaivitysnopeudenLaskemiseenKaytettavaLuokka {
    private long edellisenPaivityksenAikaleimaMillisekunteina;
    private int kuluvanSekunninRuutukuvienLaskuri;
    private int viimeksiLaskettuRuutukuvienMaaraSekunnissa;
    private long kertyneidenMillisekuntienSumma;     // time accumulator

    public KuvaruudunPaivitysnopeudenLaskemiseenKaytettavaLuokka() { }

    public void init() {
        edellisenPaivityksenAikaleimaMillisekunteina = System.currentTimeMillis();
    }

    // call this once every time the method whose frequency you want
    // to keep track of is called
    public void update() {
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

    public int getFPS() {
        return viimeksiLaskettuRuutukuvienMaaraSekunnissa;
    }
}
