package ua.kh.excel.comparison.tool.prowider;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface RowProvider extends Iterable<CellProvider> {

    default Stream<CellProvider> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    default Spliterator<CellProvider> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED);
    }

    List<String> getHeader();

    int size();

}
