package com.bkav.lk.web.rest;

import com.bkav.lk.dto.AcademicDTO;
import com.bkav.lk.service.AcademicService;
import com.bkav.lk.service.DoctorService;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AcademicResource {
    private static final String ENTITY_NAME = "Academic";

    protected AcademicService academicService;
    protected DoctorService doctorService;

    public AcademicResource(AcademicService academicService, DoctorService doctorService) {
        this.academicService = academicService;
        this.doctorService = doctorService;
    }

    /**
     * Lấy 1 bản ghi
     */
    @GetMapping("/academics/{id}")
    public ResponseEntity<AcademicDTO> getGroup(@PathVariable Long id) {
        Optional<AcademicDTO> academicDTO = academicService.findOne(id);
        if (!academicDTO.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(academicDTO);
    }

    @GetMapping("/academics")
    public ResponseEntity<List<AcademicDTO>> search(@RequestParam MultiValueMap<String, String> queryParams,
                                                    Pageable pageable) {

        Page<AcademicDTO> page = academicService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/academics")
    public ResponseEntity<AcademicDTO> create(@RequestBody @Valid AcademicDTO academicDTO) {
        if (academicDTO.getId() != null) {
            throw new BadRequestAlertException("A new Academic cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AcademicDTO result = academicService.save(academicDTO);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/academics")
    public ResponseEntity<AcademicDTO> update(@RequestBody @Valid AcademicDTO academicDTO) {
        if (academicDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AcademicDTO result = academicService.save(academicDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/academics/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Boolean isExit = doctorService.isAcademicIdExist(id);
       Optional<AcademicDTO> optionalAcademicDTO = academicService.findOne(id);
        if (!isExit) {
            academicService.delete(id);
        }else {
            throw new BadRequestAlertException(optionalAcademicDTO.get().getName(),  "is in use", "codeUsing");
        }
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, true,
                ENTITY_NAME, id.toString())).build();
    }

    /**
     * Xóa nhiều bản ghi
     */
    @PostMapping("/academics/delete")
    public ResponseEntity<Boolean> deleteListTopic(@RequestBody List<Long> listId) {
        if (listId == null) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "academic", "idNull", "Null Ids")).body(false);
        }
        for (Long id : listId) {
            Boolean isExit = doctorService.isAcademicIdExist(id);
            Optional<AcademicDTO> optionalAcademicDTO = academicService.findOne(id);

            if (!isExit) {
                academicService.delete(id);
            }else {
                throw new BadRequestAlertException( optionalAcademicDTO.get().getName(), "is in use", "codeUsing");
            }
            academicService.delete(id);
        }
        return ResponseEntity.ok().body(true);
    }

    @GetMapping("/academics/findAll")
    public ResponseEntity<List<AcademicDTO>> findAll() {
        List<AcademicDTO> results = academicService.findAll();
        return ResponseEntity.ok().body(results);
    }

    /**
     * check exist
     *
     * @param code code only
     * @return true is exist/ otherwise
     */
    @GetMapping("/academics/exist/{code}")
    public ResponseEntity<Boolean> existCode(@PathVariable String code) {
        return ResponseEntity.ok().body(academicService.existCode(code));
    }

}
