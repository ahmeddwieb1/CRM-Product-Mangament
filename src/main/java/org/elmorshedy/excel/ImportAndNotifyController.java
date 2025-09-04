package org.elmorshedy.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.*;
import org.bson.types.ObjectId;
import org.elmorshedy.email.model.EmailDetails;
import org.elmorshedy.email.service.EmailService;
import org.elmorshedy.product.model.Product;
import org.elmorshedy.product.repo.ProductRepo;
import org.elmorshedy.user.model.User;
import org.elmorshedy.user.model.UserDTO;
import org.elmorshedy.user.model.AppRole;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/import")
@Slf4j
public class ImportAndNotifyController {
    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final EmailService emailService;

    @Autowired
    public ImportAndNotifyController(UserRepo userRepo, ProductRepo productRepo, EmailService emailService) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.emailService = emailService;
    }

    @PostMapping("/excel")
    public ResponseEntity<?> importExcelAndNotify(@RequestParam("file") MultipartFile file) throws IOException {

        List<Product> productList = new ArrayList<>();
        // 1) قراءة ملف Excel
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        XSSFSheet worksheet = workbook.getSheetAt(0);

        for (int index = 1; index < worksheet.getPhysicalNumberOfRows(); index++) {
            XSSFRow row = worksheet.getRow(index);
            if (row == null) continue;

            try {
                Product product = new Product();

                // ID
                String rawId = row.getCell(0).getStringCellValue().trim();
                String cleanedId = rawId.replace("ObjectId(", "").replace(")", "");
                ObjectId id = new ObjectId(cleanedId);
                product.setId(id);

                // Name
                if (row.getCell(1) != null) {
                    product.setName(row.getCell(1).getStringCellValue().trim());
                }

                // Amount
                if (row.getCell(2) != null) {
                    product.setAmount((int) row.getCell(2).getNumericCellValue());
                }

                // Price
                if (row.getCell(3) != null) {
                    product.setPrice(row.getCell(3).getNumericCellValue());
                }

                // Description
                if (row.getCell(4) != null) {
                    product.setDescription(row.getCell(4).getStringCellValue().trim());
                }

                productList.add(product);
            } catch (Exception e) {
                log.error("❌ Failed to parse row {}: {}", index, e.getMessage());
            }
        }


        // 2) حفظ المنتجات في DB
        productRepo.saveAll(productList);

        // 3) الحصول على إيميلات الـ Sales
        List<String> salesEmails = selectSalesEmails();

        // 4) إرسال الإيميل لكل Sales
        for (String recipient : salesEmails) {
            EmailDetails details = new EmailDetails();
            details.setRecipient(recipient);
            details.setSubject("New Products Imported");
            details.setMsgBody("Hello, new products have been imported:\n\n" +
                    productList.stream()
                            .map(p -> "- " + p.getName() + " (" + p.getAmount() + " pcs)")
                            .collect(Collectors.joining("\n"))
            );

            try {
                emailService.sendSimpleMail(details);
                log.info("Email sent to {}", recipient);
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", recipient, e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "importedProducts", productList.size(),
                "emailsSentTo", salesEmails
        ));
    }
//    @PostMapping("/excel")
//    public ResponseEntity<String> importExcel(@RequestParam("file") MultipartFile file) {
//        try {
//            if (file.isEmpty()) {
//                return ResponseEntity.badRequest().body("File is empty");
//            }
//            return ResponseEntity.ok("File uploaded: " + file.getOriginalFilename());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error: " + e.getMessage());
//        }
//    }

    private List<String> selectSalesEmails() {
        return findAlluser().stream()
                .filter(user -> user.getRole() != null && user.getRole().getRolename() == AppRole.SALES_REP)
                .map(User::getEmail)
                .toList();
    }

    public List<User> findAlluser() {
        return userRepo.findAll();
    }
}
