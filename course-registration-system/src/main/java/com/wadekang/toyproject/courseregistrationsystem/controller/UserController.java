package com.wadekang.toyproject.courseregistrationsystem.controller;

import com.wadekang.toyproject.courseregistrationsystem.controller.dto.*;
import com.wadekang.toyproject.courseregistrationsystem.domain.*;
import com.wadekang.toyproject.courseregistrationsystem.service.ClassesService;
import com.wadekang.toyproject.courseregistrationsystem.service.MajorService;
import com.wadekang.toyproject.courseregistrationsystem.service.TakeClassService;
import com.wadekang.toyproject.courseregistrationsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final MajorService majorService;

    private final ClassesService classesService;

    private final TakeClassService takeClassService;

    @GetMapping("/login")
    public String login(Model model,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception) {

        model.addAttribute("error", error);
        model.addAttribute("exception", exception);

        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model,
                         @RequestParam(value="msg", required = false) String msg) {
        List<Major> majors = majorService.findAll();
        model.addAttribute("signUpDto", new UserSignUpDto());
        model.addAttribute("majors", majors);
        model.addAttribute("msg", msg);
        return "signup";
    }

    @PostMapping("/signup")
    public String create(UserSignUpDto signUpDto, Model model) {
        try {
            userService.join(signUpDto);
        } catch (Exception e) {
            List<Major> majors = majorService.findAll();

            model.addAttribute("majors", majors);
            model.addAttribute("signUpDto", signUpDto);
            model.addAttribute("msg", e.getMessage());
            return "signup";
        }

        return "redirect:/";
    }

    @GetMapping("/myCourses")   //mycourseList??? ?????? ?????? ?????? x ????????? ??????
    public String myCourseList(@AuthenticationPrincipal User user, Model model,
                               @RequestParam(value="msg", required = false) String msg) {
        UserResponseDto userResponseDto = userService.findById(user.getUserId());

        model.addAttribute("username", userResponseDto.getUsername());
        model.addAttribute("thisYearGrade",userResponseDto.getThisYearGrade());
        model.addAttribute("takeClasses",takeClassService.findNotDoneClass(user.getUserId()) ); //userResponseDto.getTakeClasses()
        model.addAttribute("msg", msg);

        return "myCourseList";
    }

    @GetMapping("/historyList")  //???????????? ????????? ?????? ??????. ????????? B0 ???????????? ????????? ??????, B0 ???????????? ???????????? ?????? ??????
    public String myHistoryList(@AuthenticationPrincipal User user, Model model,
                               @RequestParam(value="msg", required = false) String msg) {
        UserResponseDto userResponseDto = userService.findById(user.getUserId());



        model.addAttribute("username", userResponseDto.getUsername());
        model.addAttribute("averageGrade",userResponseDto.getAverageScore());
        model.addAttribute("EndTakeClasses", takeClassService.findEndClass(user.getUserId()) ); //B0 ??????, ???????????? ?????? ?????????
        model.addAttribute("NotEndTakeClasses",takeClassService.findNotEndClass(user.getUserId()));
        model.addAttribute("msg", msg);

        return "myHistoryList";
    }


    @GetMapping("/myHope")
    public String myHopeList(@AuthenticationPrincipal User user, Model model,
                             @RequestParam(value="msg", required = false) String msg) {
        UserResponseDto userResponseDto = userService.findById(user.getUserId());

        model.addAttribute("username", userResponseDto.getUsername());
        model.addAttribute("hopeClasses", userResponseDto.getHopeClasses());
        model.addAttribute("msg", msg);

        return "HopeList";
    }


    @GetMapping("/edit")
    public String update(@AuthenticationPrincipal User user, Model model, //AuthenticationPrincipal : ????????? ?????? ?????? ?????????????????????.
                         @RequestParam(value="msg", required = false) String msg) {

        UserResponseDto userResponseDto = userService.findById(user.getUserId());
        model.addAttribute("userUpdateRequestDto", new UserUpdateRequestDto(userResponseDto));
        model.addAttribute("msg", msg);

        return "edit";
    }

    @PostMapping("/edit")
    public String userInfoUpdate(@AuthenticationPrincipal User user,
                                 UserUpdateRequestDto userUpdateRequestDto, Model model) {
        try {
            userService.update(user.getUserId(), userUpdateRequestDto);
        } catch (Exception e) {
            model.addAttribute("userUpdateRequestDto", userUpdateRequestDto);
            model.addAttribute("msg", e.getMessage());
            return "edit";
        }

        return "redirect:/";
    }




//requestParam: form??? input??? ????????? ????????? ????????????.
    // ?????? /admin1 ?????? ???????????? ?????????. admin1.html??? ?????? classes??? dto??? ??????????????? ???????????????.
    @GetMapping("/admin1/{id}")   //html?????? class ?????? ?????? ?????? findbyId ??? id ?????? ?????????
    public String updateclasses( @AuthenticationPrincipal User user,@PathVariable("id") Long classId, Model model, //????????? ????????? ?????? ?????? x
                         @RequestParam(value="msg", required = false) String msg) {
       if(!user.isAaaa())  ///????????? ????????? home?????? ?????? or ?????? ?????? ( ??????: ??????????????? ??? ????????????)
           return "home";
       Long tmp=classId;
        ClassesResponseDto classesResponseDto = classesService.findById(classId); //????????? ?????? ?????? class id ??????!
        ClassUpdateRequestDto cur=new ClassUpdateRequestDto(classesResponseDto);
        cur.setClassId(classId);
        model.addAttribute("classUpdateRequestDto", cur);//savepoint1!!!!
        model.addAttribute("msg", msg);
        model.addAttribute("classId",tmp);
        return "admin1";
    }



    @PostMapping("/admin1/{id}")   //??????????????? classid??? ????????? update.  action ???????????? id??? ??????????????? ??????!
    public String classInfoUpdate(
@PathVariable("id") Long classId,
                                  ClassUpdateRequestDto classUpdateRequestDto, Model model
    ) {
        try {
            classesService.update(classId, classUpdateRequestDto);
        } catch (Exception e) {
            model.addAttribute("classUpdateRequestDto", classUpdateRequestDto);
            model.addAttribute("msg", e.getMessage());
            return "admin1";
        }
        return "redirect:/";
    }


    @GetMapping("/selectDay")
    public String selectDay(){
        return "selectDayPage";
    }

    @GetMapping("/timetable/{id}")
    public String timetable(@AuthenticationPrincipal User user, Model model,
                               @PathVariable("id") Long day,  //????????? ??????????????? ??????
                               @RequestParam(value="msg", required = false) String msg) {
        UserResponseDto userResponseDto = userService.findById(user.getUserId());
       List<TakeClass> takeClasses = takeClassService.findByDay(userResponseDto.getUserId(),day); //?????? ????????? takeclass??? ???????????? ????????? ????????? ???
       // List<TakeClass> takeClasses = userResponseDto.getTakeClasses();
       // TakeClass takeClass1 = takeClasses.get(0);
       // credits.add(takeClass1.getClasses().getCredits().get(0));
        //Credit cc=userResponseDto.getCredits().get(0);
        model.addAttribute("username", userResponseDto.getUsername());
        model.addAttribute("takeClasses", takeClasses);
        //model.addAttribute("credits",userResponseDto.getCredits()); //?????? ????????? ?????? ?????? ??????.
        model.addAttribute("day",day);
        model.addAttribute("msg", msg);

        return "timeTable";
    }


    @GetMapping("/studentList")   //??????????????? ??????. ???????????? ????????? studentCLasses??? ?????? ?????? ??????
    public String studentList( Model model,
                                     @RequestParam(value="msg", required = false) String msg) {

        List<User> users = userService.findAll();
        //user ??????

        model.addAttribute("users", users);

        model.addAttribute("msg", msg);

        return "studentList";
    }

    @GetMapping("/studentClasses/{id}")  //?????? ????????? ?????? ?????? ??????. ?????? ?????? ????????? ?????? ?????? ?????? ??????.
    public String studentCourseList( Model model,
                               @PathVariable("id") Long studentId,

                               @RequestParam(value="msg", required = false) String msg) {

        //user ??????
        UserResponseDto userResponseDto = userService.findById(studentId);
        model.addAttribute("username", userResponseDto.getUsername());
        model.addAttribute("takeClasses", userResponseDto.getTakeClasses());
        model.addAttribute("msg", msg);
        model.addAttribute("userId",userResponseDto.getUserId());
        return "studentClassList";
    }


// @PathVariable Map<String,String> pathVars,   //?????? path variable ????????????.
//String studentId=pathVars.get("studentId");
//  String classId=pathVars.get("classId");




    @GetMapping("/gradeupdate/{id}/{uid}/{cid}")   //html?????? class ?????? ?????? ?????? findbyId ??? id ?????? ?????????
    public String GradeUpdate(
            @PathVariable("id") Long takeId,
             @PathVariable("uid") Long userId,
            @PathVariable("cid") Long classId

            , Model model
    ) {
        TakeClassUpdateRequestDto takeClassUpdateRequestDto=takeClassService.findbyId(takeId);


       model.addAttribute("userId",takeClassUpdateRequestDto.getUserId());
        model.addAttribute("takeClassUpdateRequestDto", takeClassUpdateRequestDto);
        model.addAttribute("tid",takeId);
        model.addAttribute("userId",userId); //????????? ?????? ????????? model??? ?????????
        model.addAttribute("classId",classId);

        return "changeGrade";
    }

    @PostMapping("/gradeupdate/{id}/{uid}/{cid}")   //??????????????? classid??? ????????? update.  action ???????????? id??? ??????????????? ??????!
    public String GradeUpdate(  //html?????? url??? ?????? ???????????? takeid??? ???????????????.
           @PathVariable("id") Long takeId,
           @PathVariable("uid") Long userId,
          @PathVariable("cid") Long classId,
          // @ModelAttribute("userId") Long userId,
            TakeClassUpdateRequestDto takeClassUpdateRequestDto, Model model
    ) {
        try {

            //takenCLass ?????? ??????
            takeClassService.update(takeId, takeClassUpdateRequestDto); //update??? grade????????? ????????????!!!
            UserResponseDto userResponseDto =userService.findById(userId);
            UserUpdateRequestDto userUpdateRequestDto=new UserUpdateRequestDto(userResponseDto);

            //Classes ?????? ??????
            ClassesResponseDto classesResponseDto=classesService.findById(classId);//?
            ClassUpdateRequestDto classUpdateRequestDto=new ClassUpdateRequestDto(classesResponseDto);
            classesService.update(classesResponseDto.getClassId(), classUpdateRequestDto);

            if(takeClassUpdateRequestDto.getGrade()!=99L) //?????? ????????? ???????????? ???????????? default??? ???????????????, ????????????????????? ?????? curstudent ??? ??????.
                classesService.cancel(classId);


            userService.update(userResponseDto.getUserId(),userUpdateRequestDto);



        } catch (Exception e) {
            model.addAttribute("takeClassUpdateRequestDto", takeClassUpdateRequestDto);
            //model.addAttribute("msg", e.getMessage());
            return "changeGrade";
        }
        return "redirect:/studentList";
    }



}
