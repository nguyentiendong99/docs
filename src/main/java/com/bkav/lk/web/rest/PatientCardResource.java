package com.bkav.lk.web.rest;

import com.bkav.lk.dto.PatientCardDTO;
import com.bkav.lk.service.PatientCardService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class PatientCardResource {

    private static final String ENTITY_NAME = "PatientCard";

    private final PatientCardService patientCardService;

    private final UserService userService;

    @Value("${spring.application.name}")
    private String applicationName;

    public PatientCardResource(PatientCardService patientCardService, UserService userService) {
        this.patientCardService = patientCardService;
        this.userService = userService;
    }

    @PostMapping("/patient-card")
    public ResponseEntity<PatientCardDTO> create(@RequestBody @Valid PatientCardDTO patientCardDTO) throws URISyntaxException {
        PatientCardDTO patientCardDTO1 = patientCardService.findByCardNumber(patientCardDTO.getCardNumber());
        if (Objects.nonNull(patientCardDTO1)) {
            throw new BadRequestAlertException("A new patient card cannot already has card number", ENTITY_NAME, "cardnumberexist");
        }
        patientCardDTO.setUserId(userService.getUserWithAuthorities().get().getId());
        PatientCardDTO patientCard = patientCardService.save(patientCardDTO);
        return ResponseEntity.created(new URI("/api/patient-card/" + patientCard.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        patientCard.getId().toString()))
                .body(patientCard);
    }

    @GetMapping("/patient-card")
    public ResponseEntity<List<PatientCardDTO>> findAll() {
        return ResponseEntity.ok().body(patientCardService.findAll(userService.getUserWithAuthorities().get().getId()));
    }

    @PutMapping("/patient-card")
    public ResponseEntity<PatientCardDTO> update(@RequestBody @Valid PatientCardDTO patientCardDTO) {
        if (patientCardDTO.getId() == null) {
            throw new BadRequestAlertException("A PatientCard cannot save when not has ID",ENTITY_NAME,"idnull");
        }
        patientCardDTO.setUserId(userService.getUserWithAuthorities().get().getId());
        PatientCardDTO result = patientCardService.update(patientCardDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    @DeleteMapping("/patient-card/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        PatientCardDTO patientCardDTO = patientCardService.findByUserIdAndId(userService.getUserWithAuthorities().get().getId(), id);
        if (Objects.isNull(patientCardDTO)) {
            throw new BadRequestAlertException("cannot delete another user's card or cardId not exist",ENTITY_NAME,"cannotdelete");
        }
        patientCardService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true,
                ENTITY_NAME, id.toString())).build();
    }
}
