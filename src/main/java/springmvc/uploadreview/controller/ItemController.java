package springmvc.uploadreview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import springmvc.uploadreview.domain.Item;
import springmvc.uploadreview.domain.UploadName;
import springmvc.uploadreview.dto.ItemForm;
import springmvc.uploadreview.repository.ItemRepository;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ItemController {

    private final ItemRepository itemRepository;

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/items/new")
    public String itemForm() {
        return "item-form";
    }

    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes) throws IOException {
        //local storage에 저장
        UploadName attachUploadName = uploadToStorage(form.getAttachFile());

        List<UploadName> imgUploadNames = new ArrayList<>();
        for (MultipartFile imageFile : form.getImageFiles()) {
            UploadName imgUploadName = uploadToStorage(imageFile);
            imgUploadNames.add(imgUploadName);
        }

        //DB에 저장
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setAttachFile(attachUploadName);
        item.setImageFiles(imgUploadNames);
        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        return "redirect:/items/{itemId}";
    }

    @GetMapping("/items/{itemId}")
    public String itemsView(@PathVariable("itemId") Long itemId, Model model) {
        Item item = itemRepository.findOne(itemId);
        model.addAttribute("item", item);
        return "item-view";
    }

    @ResponseBody
    @GetMapping("/images/{imgStoreName}")
    private Resource getImgResource(@PathVariable("imgStoreName") String imgStoreName) throws MalformedURLException {
        //해당 경로와 이름의 파일 가져와 제공
        return new UrlResource("file:" + fileDir + imgStoreName);
    }

    @GetMapping("/attach/{itemId}")
    public ResponseEntity getAttachFileResource(@PathVariable("itemId") Long itemId) throws MalformedURLException {
        Item item = itemRepository.findOne(itemId);
        String clientName = item.getAttachFile().getClientName();
        String storageName = item.getAttachFile().getStorageName();

        UrlResource attachFile = new UrlResource("file:" + fileDir + storageName);

        //영어 제외 언어들 깨짐 문제 해결 위해 UTF_8로 인코딩
        String encodedFileName = UriUtils.encode(clientName, StandardCharsets.UTF_8);

        //파일 다운받기 위해 Content-Disposition 헤더값 넣어서 보내줘야함
        String contentDisposition = "attachment; filename=\"" + encodedFileName +"\"";

        return ResponseEntity.ok()
                .header("Content-Disposition", contentDisposition)
                .body(attachFile);
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
