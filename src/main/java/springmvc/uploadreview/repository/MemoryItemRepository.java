package springmvc.uploadreview.repository;

import org.springframework.stereotype.Repository;
import springmvc.uploadreview.domain.Item;

import java.util.HashMap;
import java.util.Map;

@Repository
public class MemoryItemRepository implements ItemRepository{

    private static Map<Long, Item> store = new HashMap<>();
    private long sequence = 0L;


    @Override
    public Long save(Item item) {
        item.setId(++sequence);
        store.put(item.getId(), item);
        return item.getId();
    }

    @Override
    public Item findOne(Long id) {
        return store.get(id);
    }
}
