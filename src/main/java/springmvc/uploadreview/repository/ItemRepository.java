package springmvc.uploadreview.repository;

import springmvc.uploadreview.domain.Item;

public interface ItemRepository {
    Long save(Item item);

    Item findOne(Long id);
}
