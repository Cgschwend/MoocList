package com.example.mooclist;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.lang.String;

//@Controller
//@RequestMapping("/")
public class JobListController {


    private JobEntityRepository jobEntityRepository;

    @RequestMapping(method = RequestMethod.GET, value = "/fail")
    public void fail() {
        throw new RuntimeException();
    }

    @ExceptionHandler(value = RuntimeException.class)
    @ResponseStatus(value = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
    public String error() {
        return "error";
    }

    @RequestMapping(method=RequestMethod.GET, value = "/jobList")
    public String jobList(Model model) {

        List<JobEntity> jobList = jobEntityRepository.findAll();
        if (jobList != null) {
            model.addAttribute("jobs", jobList);
        }
        return "jobList";
    }

}

