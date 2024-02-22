package springmvc.uploadreview.domain;

import lombok.Data;

import java.util.List;

@Data
public class Item {
    private String id;
    private String itemName;
    private UploadName attachFile;
    private List<UploadName> imageFiles;
}
