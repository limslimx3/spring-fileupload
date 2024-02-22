package springmvc.uploadreview.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import springmvc.uploadreview.domain.Item;
import springmvc.uploadreview.domain.UploadName;
import springmvc.uploadreview.dto.ItemForm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
public class ItemController {

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/items/new")
    public String itemForm() {
        return "item-form";
    }

    @ResponseBody
    @PostMapping("/items/new")
    public void saveItem(@ModelAttribute ItemForm form) throws IOException {
        //local storage에 저장
        UploadName attachUploadName = uploadToStorage(form.getAttachFile());

        List<UploadName> imgUploadNames = new ArrayList<>();
        for (MultipartFile imageFile : form.getImageFiles()) {
            UploadName imgUploadName = uploadToStorage(imageFile);
            imgUploadNames.add(imgUploadName);
        }
    }

    private UploadName uploadToStorage(MultipartFile file) throws IOException {
        String ext = extractExt(file);
        String storageFileName = createStorageFileName(ext);
        file.transferTo(new File(fileDir + storageFileName));
        return new UploadName(file.getOriginalFilename(), storageFileName);
    }


    private String createStorageFileName(String ext) {
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

}
