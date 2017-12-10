package com.example.mooclist;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class MoocListController {

    private MoocEntityRepository moocEntityRepository;
    private JobEntityRepository jobEntityRepository;
    private TagEntityRepository tagEntityRepository;

    @Autowired
    public MoocListController(MoocEntityRepository moocEntityRepository, JobEntityRepository jobEntityRepository, TagEntityRepository tagEntityRepository) {
        this.moocEntityRepository = moocEntityRepository;
        this.jobEntityRepository = jobEntityRepository;
        this.tagEntityRepository = tagEntityRepository;
    }


    @RequestMapping(value = "/fail")
    public void fail() {
        throw new RuntimeException();
    }

    @ExceptionHandler(value = RuntimeException.class)
    @ResponseStatus(value = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
    public String error() {
        return "error";
    }


    @RequestMapping(value = "/tags")
    public String tagList(Model model) {


        List<TagEntity> tagList = tagEntityRepository.findAll();
        if (tagList != null) {
            model.addAttribute("tags", tagList);
        }
        return "tagList";
    }

    @RequestMapping(value = "/alltags/{tag}")
    public String tagListAll(Model model, @PathVariable("tag") String tag) {

        if (tag != null) {
            List<TagEntity> tagListAll = tagEntityRepository.findAllByTagEquals(tag);
            if (tagListAll != null) {
                model.addAttribute("tags", tagListAll);
            }
            return "tagListAll";
        } else {
            return "redirect:/";
        }
    }

    @RequestMapping(value="job/jobDetail/{id}", method = RequestMethod.POST)
    public String tagList(Model model, @RequestParam("tags") String tag, @PathVariable("id") Integer id) {

        if (id != null) {
            List<JobEntity> jobDetail = jobEntityRepository.findAllById(id);
            if (jobDetail != null) {
                model.addAttribute("jobDetails", jobDetail);
            }
        }

        if (tag != null) {

            List<String> jobTags = Arrays.asList(tag.split("\\s*,\\s*"));
            List<MoocEntity> moocList = new ArrayList<>();

            for (String matchOnjobTag : jobTags) {

                List<TagEntity> tagList = tagEntityRepository.findAllByTagEquals(matchOnjobTag);
                if (tagList != null) {


                    for (TagEntity tagEntity : tagList) {
                        MoocEntity moocEntity = moocEntityRepository.findByIdOrderByTitle(Integer.parseInt(tagEntity.getMoocid()));
                        moocList.add(moocEntity);
                    }
                    model.addAttribute("moocs", moocList);
                }
            }

            return "jobDetail";
        } else {
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/")
    public String jobList(Model model) {

        List<JobEntity> jobList = jobEntityRepository.findAll();
        if (jobList != null) {
            model.addAttribute("jobs", jobList);
        }
        return "jobList";
    }


    @RequestMapping(value="job/jobDetail/{id}", method = RequestMethod.GET)
    public String jobDetail(Model model, @PathVariable("id") Integer id) {

        if (id != null) {
            List<JobEntity> jobDetail = jobEntityRepository.findAllById(id);
            if (jobDetail != null) {
                model.addAttribute("jobDetails", jobDetail);
            }
            return "jobDetail";
        } else {
            return "redirect:/jobs";
        }

    }

    @RequestMapping("/moocs")
    public String moocList(Model model) {

        List<MoocEntity> moocList = moocEntityRepository.findAllByOrderByTitle();
        if (moocList != null) {
            model.addAttribute("moocs", moocList);
        }
        return "moocList";
    }



    @RequestMapping(value = "/moocList", method = RequestMethod.POST)
    public String filterMoocList(Model model, String provider, String subjectTags) {


        if (provider != null) {
            List<MoocEntity> moocList = moocEntityRepository.findAllByProviderOrderByTitle(provider);
            model.addAttribute("moocs", moocList);
            return "moocList";
        } else {

            if (subjectTags != null) {
                List<MoocEntity> moocList = moocEntityRepository.findAllBySubjectTagsLikeOrderByTitle(subjectTags);
                model.addAttribute("moocs", moocList);
                return "moocList";
            } else {
                return "redirect:/moocList";
            }
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String meetup(Model model) throws IOException {


        List<JobEntity> jobList = jobEntityRepository.findAll();
        if (jobList != null) {
            model.addAttribute("jobs", jobList);
        }

        String meetupList = "Let's find local meetups";

        try {

            meetupList = getJSON("https://api.meetup.com/2/groups?key=5c21234fc612766e5e776337582036&zip=72758&radius=50&topic=SoftwareDev&sign=true",
                    5000);

        } finally {

        }

        Gson gson = new Gson();
        Meetup meetup = gson.fromJson(meetupList, Meetup.class);

        List<Group> meetupGroups = meetup.getResults();

        model.addAttribute("meetups", meetupGroups);
        return "jobList";

    }



    public String getJSON(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }

}

