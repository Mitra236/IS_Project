package ib.project.upload;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.unzip.UnzipUtil;


@RestController
@RequestMapping("api/uploadZip")
@CrossOrigin("*")
public class UploadController {

    //Save the uploaded file to this folder
    private static String UPLOADED_FOLDER = "C://Users//mitra//IS_Project//IS_Project//IB_Project_Shell//IB_Project_Shell//src//main//resources//files//";

   //IB_Project_Shell\IB_Project_Shell
    @GetMapping("/")
    public String index() {
        return "upload";
    }

    @PostMapping("/upload") // //new annotation since 4.3
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                                  RedirectAttributes redirectAttributes) {

        System.out.println(file.getOriginalFilename());

        if (file.isEmpty()) {
            System.out.println("IS IT EMPTY?");
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {

            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            System.out.println(UPLOADED_FOLDER);
            Files.write(path, bytes);

            try {
                unCompressPasswordProtectedFiles(UPLOADED_FOLDER + "images.zip");
            }catch (ZipException e){
                e.printStackTrace();
            }

            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }

        //return new ResponseEntity<>(HttpStatus.OK);
        return "redirect:uploadStatus";
       // return "Success";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }

    private void unCompressPasswordProtectedFiles(String sourcePath) throws ZipException{
        String destPath = UPLOADED_FOLDER;
        System.out.println("Destination " + destPath);
        ZipFile zipFile = new ZipFile(sourcePath);
        // If it is encrypted then provide password
        if(zipFile.isEncrypted()){
            zipFile.setPassword("123");
        }
        zipFile.extractAll(destPath);
    }

}