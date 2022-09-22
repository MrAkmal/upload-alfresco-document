package com.example.uploadalfrescodocument.dispute;

import com.example.uploadalfrescodocument.dto.CommonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/dispute")
@CrossOrigin("*")
public class DisputeDocumentController {


    private final DisputeDocumentService service;

    @Autowired
    public DisputeDocumentController(DisputeDocumentService service) {
        this.service = service;
    }



    @GetMapping
    public ResponseEntity<List<CommonDTO>> getAll(){
        return service.getAll();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        return service.delete(id);
    }


    @GetMapping("/{id}/{version}")
    public @ResponseBody ResponseEntity download(@PathVariable Long id,@PathVariable String version) {
        return service.download(id,version);
    }



}
