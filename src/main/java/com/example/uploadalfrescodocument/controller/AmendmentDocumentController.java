package com.example.uploadalfrescodocument.controller;

import com.example.uploadalfrescodocument.service.AmendmentDocumentService;
import com.example.uploadalfrescodocument.dto.CommonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/amendment")
@CrossOrigin("*")
public class AmendmentDocumentController {



    private final AmendmentDocumentService service;

    @Autowired
    public AmendmentDocumentController(AmendmentDocumentService service) {
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
