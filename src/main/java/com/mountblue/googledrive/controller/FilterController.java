package com.mountblue.googledrive.controller;


import com.mountblue.googledrive.entity.File;
import com.mountblue.googledrive.entity.Users;
import com.mountblue.googledrive.service.FileService;
import com.mountblue.googledrive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Controller
public class FilterController {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @GetMapping("/filter")
    public String filter( @RequestParam(name = "minSize", required = false) Long minSize,
                          @RequestParam(name = "maxSize", required = false) Long maxSize,
                          @RequestParam(name = "fileName", required = false) String fileName,
                          @RequestParam(name = "fileType", required = false) String fileType,
                         Principal principal,
                         Model model){
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();
        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);

        if(fileType!=null && fileType.trim().equals("")){
            fileType = null;
        }
        Set<File> filteredFiles = fileService.filterFiles(minSize, maxSize, fileName, fileType);

        Iterator<File> iterator = filteredFiles.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            if (file.getUser() != user) {
                iterator.remove();  // Safe removal using Iterator
            }
        }
        model.addAttribute("files",filteredFiles);
        System.out.println(filteredFiles);
        return "filtered-files";
    }
}
