package com.wadekang.toyproject.courseregistrationsystem.service;

import com.wadekang.toyproject.courseregistrationsystem.controller.dto.ClassesResponseDto;
import com.wadekang.toyproject.courseregistrationsystem.controller.dto.ClassUpdateRequestDto;

import com.wadekang.toyproject.courseregistrationsystem.controller.dto.UserResponseDto;
import com.wadekang.toyproject.courseregistrationsystem.controller.dto.UserSignUpDto;
import com.wadekang.toyproject.courseregistrationsystem.domain.*;

import com.wadekang.toyproject.courseregistrationsystem.repository.ClassesRepository;
import com.wadekang.toyproject.courseregistrationsystem.repository.CourseRepository;
import com.wadekang.toyproject.courseregistrationsystem.repository.RoomRepository;
import com.wadekang.toyproject.courseregistrationsystem.repository.TakeClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassesService {

    private final ClassesRepository classesRepository;
    private final CourseRepository courseRepository;

    private final TakeClassRepository takeClassRepository;

    private final RoomRepository roomRepository;

    public ClassesResponseDto findById(Long classId) {
        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Failed: No Classes Info"));

        return new ClassesResponseDto(classes);
    }

    /*
    public ClassesResponseDto findByName(String classesName) {
        List<Classes> classes = classesRepository.findByNameContaining(classesName)
                .orElseThrow(() -> new IllegalArgumentException("Failed: No Classes Info"));

        return new ClassesResponseDto(classes);
    }

     */



    @Transactional
    public Long update(Long classId, ClassUpdateRequestDto requestDto) {
        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Failed: No Class Info"));

        Long  ans1= takeClassRepository.findClassAverage(classId);
               // .orElseThrow(() -> new IllegalArgumentException("Failed: no avgClassScore Info"));
        Long  ans2= takeClassRepository.findClassUserAverage(classId);
                //.orElseThrow(() -> new IllegalArgumentException("Failed: no avgClassUserScore Info"));
       // Long ans= avg.getFirst()-avg.getSecond(); //????????? ????????? ????????? ??????????????? ?????? -?????? ????????? ????????????


        if(ans1!=null && ans2!=null) {
            Long ans = ans2 - ans1; //?????? ?????? ?????? ????????? ?????? ????????? ?????? - ?????? ???????????? ???????????? ?????? ??????
            requestDto.setAverageScore(ans);
        }


        classes.update(requestDto);

        return classId;
    }


    @Transactional
    public Long save(Long courseId ,Long roomId,ClassUpdateRequestDto classes) {


        //if (classes.isFull()) throw new IllegalArgumentException("Failed: Full"); //???????????? ?????????.

        Course course= courseRepository.findById(courseId).get();
        Room room =roomRepository.findById(roomId).get();




        if(room.getMaxPerson()<classes.getMaxStudentNum()) throw new IllegalArgumentException("Failed: Over Class Size"); //?????? ?????? ??????

        Classes tmpClasses = classesRepository.save(
                Classes.builder()
                        .course(course)  //????????? ?????? ???????????? ???????????????, getCourse() ??? ???????????????!
                        .classNumber(classes.getClassNumber())
                        .professorName(classes.getProfessorName())
                        .maxStudentNum(classes.getMaxStudentNum())
                        .curStudentNum(classes.getCurStudentNum())
                        .room(room)
                        .Day(classes.getDay())
                        .startTime(classes.getStartTime())
                        .endTime(classes.getEndTime())
                        .gradeAmount(classes.getGradeAmount())
                        .build());




        return tmpClasses.getClassId();
    }






    @Transactional
    public void delete(Long classId) { //cancel ??? ???????????? ( ?????? ?????? ??????)


        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Failed: No Class Info"));

      classesRepository.delete(classes);




    }

    @Transactional
    public void cancel(Long classId){
        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Failed: No Class Info"));
      //  classes.cancel();
        classes.SetCurStudent(takeClassRepository.findNumOfCurStudent(classes.getClassId()) );

    }

    public List<Classes> findAll() {
        return classesRepository.findAll();
    }
/*
    public Classes findById(Long classId) {
        return classesRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Failed: No Class Info"));
    }
*/


    public List<Classes> findByCourse(Long courseId) { //classdto??? ?????????.
        return classesRepository.findByCourse(courseId);
    }

    public List<Classes> findTopTen(){return classesRepository.findTopTen(); }


    public List<Classes> findByMajorAndKeyword(Long majorId,String keyword) {
        return classesRepository.findByMajorAndKeyword(majorId,keyword);
    }



    /*
    public List<ClassesResponseDto> findByCourse(Long courseId) { //classdto??? ?????????.
        List<Classes> classes=classesRepository.findByCourse(courseId);
        return new ClassesResponseDto(classes);



    }

    public UserResponseDto findById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Failed: No User Info"));

        return new UserResponseDto(user);
    }


     */




}
