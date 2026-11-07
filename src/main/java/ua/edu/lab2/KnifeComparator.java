package ua.edu.lab2;

import java.util.Comparator;

/** Сортування за довжиною клинка, а за рівності — за ID. */
public class KnifeComparator implements Comparator<Knife> {
    @Override
    public int compare(Knife first, Knife second) {
        int result = Double.compare(first.visual().lengthCm(), second.visual().lengthCm());
        return result != 0 ? result : first.id().compareTo(second.id());
    }
}
