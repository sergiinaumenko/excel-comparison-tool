package ua.kh.excel.provider;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface CellProvider extends Iterable<Pair<Cell, Cell>> {

    default Stream<Pair<Cell, Cell>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    default Spliterator<Pair<Cell, Cell>> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED);
    }

    Pair<Cell, Cell> getCell(int cellNum);

    Pair<String, String> getRowTitle();

    Iterator<Pair<Cell, Cell>> iterator();

    int size();

}
