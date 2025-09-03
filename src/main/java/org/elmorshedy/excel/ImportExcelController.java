package org.elmorshedy.excel;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.elmorshedy.product.model.Product;
import org.elmorshedy.product.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@RestController
public class ImportExcelController {

    private final ProductRepo productRepo;
    @Autowired
    public ImportExcelController(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    @PostMapping("/import-excel")
    public ResponseEntity<List<Product>> importExcelFile(@RequestParam("file") MultipartFile files) throws IOException {
        HttpStatus status = HttpStatus.OK;
        List<Product> productList = new ArrayList<>();

        XSSFWorkbook workbook = new XSSFWorkbook(files.getInputStream());
        XSSFSheet worksheet = workbook.getSheetAt(0);

        for (int index = 0; index < worksheet.getPhysicalNumberOfRows(); index++) {
            if (index > 0) {
                Product product = new Product();

                XSSFRow row = worksheet.getRow(index);

                String rawId = row.getCell(0).getStringCellValue();
                String cleanedId = rawId.replace("ObjectId(", "").replace(")", "");
                ObjectId id = new ObjectId(cleanedId);

                product.setId(id);
                product.setName(row.getCell(1).getStringCellValue());
                product.setAmount((int) row.getCell(2).getNumericCellValue());
                product.setPrice(row.getCell(3).getNumericCellValue());
                product.setDescription(row.getCell(4).getStringCellValue());

                productList.add(product);
            }
        }

        productRepo.saveAll(productList);

        return new ResponseEntity<>(productList, status);
    }
}